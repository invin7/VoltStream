// All simulation logic lives in the Java server. This file sends actions and draws the state it returns.

const BAY_COLOR_COUNT = 3;

const page = {
  clock: document.getElementById('clock'),
  revenue: document.getElementById('revenue'),
  step: document.getElementById('step'),
  reset: document.getElementById('reset'),
  banner: document.getElementById('banner'),
  feed: document.querySelector('.feed'),
  loadKw: document.getElementById('load-kw'),
  capacityKw: document.getElementById('capacity-kw'),
  loadPercent: document.getElementById('load-percent'),
  loadNote: document.getElementById('load-note'),
  meter: document.getElementById('meter'),
  meterPeak: document.getElementById('meter-peak'),
  meterTick: document.getElementById('meter-tick'),
  meterPeakLabel: document.getElementById('meter-peak-label'),
  segments: document.getElementById('segments'),
  legend: document.getElementById('legend'),
  bays: document.getElementById('bays'),
  bayTemplate: document.getElementById('bay-template'),
  form: document.getElementById('vehicle-form'),
  vehicleTypes: document.getElementById('vehicle-types'),
  plate: document.getElementById('plate'),
  formError: document.getElementById('form-error'),
  activity: document.getElementById('activity'),
  activityEmpty: document.getElementById('activity-empty'),
};

const rupees = new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' });

let bayViews = null;
let busy = false;

function formatKw(value) {
  return `${value.toFixed(1)} kW`;
}

function formatClock(totalMinutes) {
  const hours = String(Math.floor(totalMinutes / 60)).padStart(2, '0');
  const minutes = String(Math.round(totalMinutes % 60)).padStart(2, '0');
  return `T+${hours}:${minutes}`;
}

function formatDuration(hours) {
  const totalMinutes = Math.ceil(hours * 60);
  if (totalMinutes < 60) return `${totalMinutes} min`;
  const minutes = totalMinutes % 60;
  return minutes === 0 ? `${totalMinutes / 60} h` : `${Math.floor(totalMinutes / 60)} h ${minutes} min`;
}

async function api(path, body) {
  let response;
  try {
    response = await fetch(path, { method: path === '/api/state' ? 'GET' : 'POST', body });
  } catch {
    throw new Error('offline');
  }

  const data = await response.json();
  if (!response.ok) {
    const error = new Error(data.error);
    error.fromServer = true;
    throw error;
  }
  return data;
}

// Runs one action at a time so responses can't arrive out of order and redraw stale state.
async function run(path, body) {
  if (busy) return;
  busy = true;

  try {
    const state = await api(path, body);
    page.banner.hidden = true;
    render(state);
    return true;
  } catch (error) {
    if (error.fromServer) throw error;
    page.banner.textContent = 'Could not reach the VoltStream server. Check that it is still running, then try again.';
    page.banner.hidden = false;
  } finally {
    busy = false;
  }
}

function render(state) {
  if (!bayViews) buildStation(state);

  page.clock.textContent = formatClock(state.stepCount * state.stepMinutes);
  page.revenue.textContent = rupees.format(state.revenue);
  page.step.textContent = `Advance ${Math.round(state.stepMinutes)} min`;

  renderGrid(state.grid, state.bays);
  state.bays.forEach((bay, index) => renderBay(bayViews[index], bay));
  renderActivity(state);
}

function buildStation(state) {
  bayViews = state.bays.map((bay, index) => {
    const color = `var(--bay-${(index % BAY_COLOR_COUNT) + 1})`;

    const card = page.bayTemplate.content.firstElementChild.cloneNode(true);
    card.style.setProperty('--bay-color', color);
    const fields = {};
    card.querySelectorAll('[data-field]').forEach((element) => {
      fields[element.dataset.field] = element;
    });
    fields.id.textContent = bay.id;
    fields.spec.textContent = `${bay.typeName}, ${bay.maxOutputKw} kW max`;
    page.bays.append(card);

    const segment = document.createElement('div');
    segment.className = 'meter-segment';
    segment.style.setProperty('--bay-color', color);
    page.segments.append(segment);

    const legendItem = document.createElement('li');
    legendItem.style.setProperty('--bay-color', color);
    const legendKw = document.createElement('strong');
    legendItem.append(`${bay.id} `, legendKw);
    page.legend.append(legendItem);

    return { card, fields, segment, legendKw };
  });

  state.vehicleTypes.forEach((type, index) => {
    const option = document.createElement('label');
    option.className = 'type-option';

    const input = document.createElement('input');
    input.type = 'radio';
    input.name = 'type';
    input.value = type.id;
    input.checked = index === 0;

    const name = document.createElement('span');
    name.textContent = type.name;

    const spec = document.createElement('span');
    spec.className = 'type-spec';
    spec.textContent = `${type.batteryKwh} kWh · ${type.maxRateKw} kW`;

    option.append(input, name, spec);
    page.vehicleTypes.append(option);
  });

  const peakPercent = state.grid.peakThreshold * 100;
  page.meterPeak.style.left = `${peakPercent}%`;
  page.meterTick.style.left = `${peakPercent}%`;
  page.meterPeakLabel.textContent = `Peak pricing above ${peakPercent}%`;
}

function renderGrid(grid, bays) {
  const loadPercent = grid.loadFactor * 100;

  page.loadKw.textContent = grid.allocatedKw.toFixed(1);
  page.capacityKw.textContent = grid.capacityKw;
  page.loadPercent.textContent = `${Math.round(loadPercent)}% load`;

  const oversubscribed = grid.requestedKw > grid.capacityKw;
  const peak = grid.loadFactor > grid.peakThreshold;
  page.feed.dataset.level = oversubscribed ? 'over' : peak ? 'peak' : 'normal';

  if (oversubscribed) {
    const scale = Math.round((grid.capacityKw / grid.requestedKw) * 100);
    page.loadNote.textContent = `${formatKw(grid.requestedKw)} requested, every bay scaled to ${scale}%`;
  } else if (peak) {
    page.loadNote.textContent = 'Peak tariffs active';
  } else if (grid.allocatedKw > 0) {
    page.loadNote.textContent = 'Standard tariffs';
  } else {
    page.loadNote.textContent = 'No vehicles charging';
  }

  page.meter.setAttribute('aria-label', `Grid load ${Math.round(loadPercent)} percent of ${grid.capacityKw} kilowatts`);

  bays.forEach((bay, index) => {
    const view = bayViews[index];
    view.segment.style.width = `${(bay.allocatedKw / grid.capacityKw) * 100}%`;
    view.legendKw.textContent = bay.vehicle ? formatKw(bay.allocatedKw) : 'idle';
  });
}

function renderBay(view, bay) {
  const { card, fields } = view;
  const vehicle = bay.vehicle;

  fields.idle.hidden = Boolean(vehicle);
  fields.body.hidden = !vehicle;

  if (!vehicle) {
    card.dataset.state = 'idle';
    fields.status.textContent = 'Available';
    fields.fill.style.width = '0';
    return;
  }

  const throttled = bay.allocatedKw < bay.requestedKw - 1e-9;
  card.dataset.state = throttled ? 'throttled' : 'charging';
  fields.status.textContent = throttled ? 'Throttled' : 'Charging';

  fields.plate.textContent = vehicle.plate;
  fields.plate.title = vehicle.plate;
  fields.vehicleName.textContent = vehicle.typeName;

  fields.fill.style.width = `${vehicle.chargePercent}%`;
  fields.battery.setAttribute('aria-valuenow', vehicle.chargePercent.toFixed(1));
  fields.battery.setAttribute('aria-label', `${vehicle.plate} battery level`);
  fields.percent.textContent = `${vehicle.chargePercent.toFixed(1)}%`;
  fields.energy.textContent = `${vehicle.currentKwh.toFixed(1)} / ${vehicle.batteryKwh} kWh`;

  fields.power.textContent = throttled
    ? `${formatKw(bay.allocatedKw)} of ${bay.requestedKw}`
    : formatKw(bay.allocatedKw);
  fields.tariff.textContent = `${rupees.format(bay.tariff)}/kWh`;
  fields.tariff.dataset.peak = bay.surcharged;
  fields.eta.textContent = formatDuration((vehicle.batteryKwh - vehicle.currentKwh) / bay.allocatedKw);
}

function renderActivity(state) {
  page.activityEmpty.hidden = state.events.length > 0;

  const items = state.events.map((event) => {
    const item = document.createElement('li');
    item.dataset.kind = event.kind;

    const time = document.createElement('time');
    time.textContent = formatClock(event.step * state.stepMinutes);

    const message = document.createElement('span');
    message.textContent = event.message;

    item.append(time, message);
    return item;
  });

  page.activity.replaceChildren(...items);
}

page.step.addEventListener('click', () => {
  page.formError.hidden = true;
  run('/api/step');
});

page.reset.addEventListener('click', () => {
  page.formError.hidden = true;
  run('/api/reset');
});

page.form.addEventListener('submit', async (event) => {
  event.preventDefault();

  try {
    const added = await run('/api/vehicles', new URLSearchParams(new FormData(page.form)));
    page.formError.hidden = true;
    if (added) {
      page.plate.value = '';
      page.plate.focus();
    }
  } catch (error) {
    page.formError.textContent = error.message;
    page.formError.hidden = false;
  }
});

run('/api/state');
