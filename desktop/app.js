/**
 * JobAutoApply Desktop Core Controller
 * Supports direct execution, simulation, bookmarklet generation, and answers vault.
 */

// --- State Management ---
const AppState = {
  activePlatform: 'instahyre', // 'instahyre' | 'linkedin'
  isRunning: false,
  isPaused: false,
  appliedCount: 0,
  skippedCount: 0,
  timerId: null,
  
  profile: {
    fullName: "Job Seeker",
    phoneNumber: "+919876543210",
    totalExperienceYears: 3.5,
    noticePeriodDays: 30,
    currentCtcLakhs: 12,
    expectedCtcLakhs: 18,
    isAuthorizedToWork: true,
    requiresVisaSponsorship: false,
    preferredLocations: "Bangalore, Remote, Pune, Hyderabad"
  },

  settings: {
    minDelay: 2.5,
    maxDelay: 4.5,
    maxApplications: 30,
    skipLowMatch: true,
    autoBulkApply: true,
    skipExternal: true
  },

  history: [],
  logs: []
};

// --- Initialization ---
document.addEventListener('DOMContentLoaded', () => {
  loadSavedData();
  setupNavigation();
  updateBookmarkletCode();
  renderDashboard();
  addLog("System initialized and ready.", "info");
});

// --- Local Storage Loading & Saving ---
function loadSavedData() {
  const savedProfile = localStorage.getItem('job_apply_profile');
  if (savedProfile) {
    try { AppState.profile = Object.assign(AppState.profile, JSON.parse(savedProfile)); } catch(e){}
  }

  const savedSettings = localStorage.getItem('job_apply_settings');
  if (savedSettings) {
    try { AppState.settings = Object.assign(AppState.settings, JSON.parse(savedSettings)); } catch(e){}
  }

  const savedHistory = localStorage.getItem('job_apply_history');
  if (savedHistory) {
    try { AppState.history = JSON.parse(savedHistory); } catch(e){}
  }

  // Populate UI inputs
  document.getElementById('profFullName').value = AppState.profile.fullName || '';
  document.getElementById('profPhone').value = AppState.profile.phoneNumber || '';
  document.getElementById('profExp').value = AppState.profile.totalExperienceYears || 3;
  document.getElementById('profNotice').value = AppState.profile.noticePeriodDays || 30;
  document.getElementById('profCurrentCtc').value = AppState.profile.currentCtcLakhs || 12;
  document.getElementById('profExpectedCtc').value = AppState.profile.expectedCtcLakhs || 18;
  document.getElementById('profAuthorized').value = String(AppState.profile.isAuthorizedToWork);
  document.getElementById('profSponsorship').value = String(AppState.profile.requiresVisaSponsorship);
  document.getElementById('profLocations').value = AppState.profile.preferredLocations || '';

  document.getElementById('minDelayRange').value = AppState.settings.minDelay;
  document.getElementById('maxDelayRange').value = AppState.settings.maxDelay;
  document.getElementById('maxAppsInput').value = AppState.settings.maxApplications;
  document.getElementById('skipLowMatchSelect').value = String(AppState.settings.skipLowMatch);
  document.getElementById('autoBulkSelect').value = String(AppState.settings.autoBulkApply);
  document.getElementById('skipExternalSelect').value = String(AppState.settings.skipExternal);

  updateDelayLabels();
  renderHistoryTable();
}

function saveProfile(e) {
  if (e) e.preventDefault();
  AppState.profile = {
    fullName: document.getElementById('profFullName').value,
    phoneNumber: document.getElementById('profPhone').value,
    totalExperienceYears: parseFloat(document.getElementById('profExp').value) || 3,
    noticePeriodDays: parseInt(document.getElementById('profNotice').value) || 30,
    currentCtcLakhs: parseFloat(document.getElementById('profCurrentCtc').value) || 12,
    expectedCtcLakhs: parseFloat(document.getElementById('profExpectedCtc').value) || 18,
    isAuthorizedToWork: document.getElementById('profAuthorized').value === 'true',
    requiresVisaSponsorship: document.getElementById('profSponsorship').value === 'true',
    preferredLocations: document.getElementById('profLocations').value
  };

  localStorage.setItem('job_apply_profile', JSON.stringify(AppState.profile));
  updateBookmarkletCode();
  
  const alert = document.getElementById('profileSavedAlert');
  alert.style.display = 'inline';
  setTimeout(() => { alert.style.display = 'none'; }, 2500);
  addLog("Profile answers updated and saved to storage.", "success");
}

function saveSettings(e) {
  if (e) e.preventDefault();
  AppState.settings = {
    minDelay: parseFloat(document.getElementById('minDelayRange').value) || 2.5,
    maxDelay: parseFloat(document.getElementById('maxDelayRange').value) || 4.5,
    maxApplications: parseInt(document.getElementById('maxAppsInput').value) || 30,
    skipLowMatch: document.getElementById('skipLowMatchSelect').value === 'true',
    autoBulkApply: document.getElementById('autoBulkSelect').value === 'true',
    skipExternal: document.getElementById('skipExternalSelect').value === 'true'
  };

  localStorage.setItem('job_apply_settings', JSON.stringify(AppState.settings));
  updateBookmarkletCode();
  renderDashboard();

  const alert = document.getElementById('settingsSavedAlert');
  alert.style.display = 'inline';
  setTimeout(() => { alert.style.display = 'none'; }, 2500);
  addLog("Safety & pacing rules updated.", "success");
}

function updateDelayLabels() {
  const min = document.getElementById('minDelayRange').value;
  const max = document.getElementById('maxDelayRange').value;
  document.getElementById('minDelayLabel').innerText = `${min}s`;
  document.getElementById('maxDelayLabel').innerText = `${max}s`;
  document.getElementById('delayRangeVal').innerText = `${min} - ${max}s`;
}

// --- Navigation Tabs ---
function setupNavigation() {
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => {
    item.addEventListener('click', () => {
      navItems.forEach(n => n.classList.remove('active'));
      item.classList.add('active');

      const targetTab = item.getAttribute('data-tab');
      document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('active'));
      const activePane = document.getElementById(`tab-${targetTab}`);
      if (activePane) activePane.classList.add('active');
    });
  });
}

// --- Platform Switcher ---
function setPlatform(platform) {
  AppState.activePlatform = platform;
  const instaBtn = document.getElementById('selectInstahyre');
  const linkedinBtn = document.getElementById('selectLinkedin');
  const nameEl = document.getElementById('activePlatformName');

  if (platform === 'instahyre') {
    instaBtn.className = 'platform-btn active instahyre';
    linkedinBtn.className = 'platform-btn linkedin';
    nameEl.innerText = 'Instahyre';
    nameEl.style.color = 'var(--instahyre-teal)';
  } else {
    instaBtn.className = 'platform-btn instahyre';
    linkedinBtn.className = 'platform-btn active linkedin';
    nameEl.innerText = 'LinkedIn Easy Apply';
    nameEl.style.color = 'var(--linkedin-blue)';
  }

  updateBookmarkletCode();
  addLog(`Switched active platform target to ${nameEl.innerText}.`, "info");
}

// --- Direct Launch ---
function openDirectJobTab() {
  const url = AppState.activePlatform === 'instahyre' 
    ? 'https://www.instahyre.com/candidate/opportunities/' 
    : 'https://www.linkedin.com/jobs/';
  window.open(url, '_blank');
  addLog(`Launched ${url} in a new tab. Click your bookmarklet once on the page!`, "info");
}

// --- Logging ---
function addLog(msg, level = 'info') {
  const time = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  const row = document.createElement('div');
  row.className = 'log-row';
  row.innerHTML = `
    <span class="log-time">[${time}]</span>
    <span class="log-badge ${level}">${level}</span>
    <span class="log-msg">${escapeHtml(msg)}</span>
  `;

  const terminal = document.getElementById('logTerminal');
  terminal.appendChild(row);
  terminal.scrollTop = terminal.scrollHeight;
}

function clearLogs() {
  document.getElementById('logTerminal').innerHTML = '';
  addLog("Logs cleared.", "info");
}

function escapeHtml(text) {
  return String(text).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

// --- Render Dashboard ---
function renderDashboard() {
  document.getElementById('appliedCountVal').innerText = AppState.appliedCount;
  document.getElementById('skippedCountVal').innerText = AppState.skippedCount;
  document.getElementById('sessionLimitVal').innerText = AppState.settings.maxApplications;
  document.getElementById('delayRangeVal').innerText = `${AppState.settings.minDelay} - ${AppState.settings.maxDelay}s`;
}

// --- History Tracking ---
function recordJob(title, company, platform, status, note = '') {
  const item = {
    id: Date.now(),
    time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    date: new Date().toLocaleDateString(),
    title,
    company,
    platform,
    status,
    note
  };

  AppState.history.unshift(item);
  localStorage.setItem('job_apply_history', JSON.stringify(AppState.history));
  renderHistoryTable();

  if (status === 'Applied') {
    AppState.appliedCount++;
  } else {
    AppState.skippedCount++;
  }
  renderDashboard();
}

function renderHistoryTable() {
  const tbody = document.getElementById('historyTableBody');
  if (AppState.history.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; padding: 36px; color: var(--text-muted);">No applications logged yet. Run the bot to view submitted jobs here.</td></tr>`;
    return;
  }

  tbody.innerHTML = AppState.history.map(item => {
    const statusColor = item.status === 'Applied' ? 'var(--emerald-success)' : 'var(--amber-warning)';
    return `
      <tr>
        <td style="color: var(--text-muted);">${item.date} ${item.time}</td>
        <td style="font-weight: 600; color: var(--text-primary);">${escapeHtml(item.title)}</td>
        <td>${escapeHtml(item.company)}</td>
        <td><span style="font-weight: 600;">${item.platform}</span></td>
        <td><span style="color: ${statusColor}; font-weight: 600;">${item.status}</span></td>
      </tr>
    `;
  }).join('');
}

function clearHistory() {
  if (confirm("Clear all recorded application history?")) {
    AppState.history = [];
    localStorage.removeItem('job_apply_history');
    renderHistoryTable();
    addLog("Application history cleared.", "warning");
  }
}

function exportHistoryCsv() {
  if (AppState.history.length === 0) {
    alert("No history to export.");
    return;
  }

  let csv = "Date,Time,Title,Company,Platform,Status,Note\n";
  AppState.history.forEach(item => {
    csv += `"${item.date}","${item.time}","${item.title.replace(/"/g, '""')}","${item.company.replace(/"/g, '""')}","${item.platform}","${item.status}","${item.note || ''}"\n`;
  });

  const blob = new Blob([csv], { type: 'text/csv' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `JobAutoApply_History_${new Date().toISOString().slice(0,10)}.csv`;
  a.click();
  URL.revokeObjectURL(url);
  addLog("Exported history to CSV.", "success");
}

// --- Bot Simulation Controls ---
function toggleBotExecution() {
  const btn = document.getElementById('startBotBtn');
  const badge = document.getElementById('statusBadge');
  const statusText = document.getElementById('statusText');
  const actionTitle = document.getElementById('currentActionTitle');

  if (!AppState.isRunning) {
    AppState.isRunning = true;
    AppState.isPaused = false;
    badge.className = 'status-badge running';
    statusText.innerText = 'Running';
    btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/></svg><span>Pause</span>`;
    actionTitle.innerText = `Autonomous Engine Active (${AppState.activePlatform.toUpperCase()})`;
    addLog(`Autonomous loop started for ${AppState.activePlatform}. Scanning for job cards...`, "info");
    runSimulatedCycle();
  } else if (!AppState.isPaused) {
    AppState.isPaused = true;
    badge.className = 'status-badge paused';
    statusText.innerText = 'Paused';
    btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg><span>Resume</span>`;
    actionTitle.innerText = "Engine Paused by User";
    addLog("Automation paused.", "warning");
    if (AppState.timerId) clearTimeout(AppState.timerId);
  } else {
    AppState.isPaused = false;
    badge.className = 'status-badge running';
    statusText.innerText = 'Running';
    btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/></svg><span>Pause</span>`;
    actionTitle.innerText = `Autonomous Engine Active (${AppState.activePlatform.toUpperCase()})`;
    addLog("Automation resumed.", "info");
    runSimulatedCycle();
  }
}

function stopBotExecution() {
  AppState.isRunning = false;
  AppState.isPaused = false;
  if (AppState.timerId) clearTimeout(AppState.timerId);

  const btn = document.getElementById('startBotBtn');
  const badge = document.getElementById('statusBadge');
  const statusText = document.getElementById('statusText');
  const actionTitle = document.getElementById('currentActionTitle');

  badge.className = 'status-badge';
  statusText.innerText = 'Ready';
  btn.innerHTML = `<svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg><span>Start Bot</span>`;
  actionTitle.innerText = "Automation Stopped";
  addLog("Automation stopped.", "warning");
}

function runSimulatedCycle() {
  if (!AppState.isRunning || AppState.isPaused) return;

  const min = AppState.settings.minDelay * 1000;
  const max = AppState.settings.maxDelay * 1000;
  const delay = Math.floor(Math.random() * (max - min + 1)) + min;

  AppState.timerId = setTimeout(() => {
    simulateStep();
    if (AppState.isRunning && !AppState.isPaused) {
      runSimulatedCycle();
    }
  }, delay);
}

const sampleRoles = [
  "Senior Full Stack Engineer", "React & Node.js Developer", "Software Engineer - Backend",
  "Frontend Developer (TypeScript)", "Staff Software Engineer", "Android / Mobile Specialist"
];
const sampleCompanies = [
  "Razorpay", "Swiggy", "CRED", "PhonePe", "BrowserStack", "Postman", "Zomato", "Groww"
];

function simulateStep() {
  const role = sampleRoles[Math.floor(Math.random() * sampleRoles.length)];
  const comp = sampleCompanies[Math.floor(Math.random() * sampleCompanies.length)];
  const isApplied = Math.random() > 0.25;

  if (isApplied) {
    addLog(`Found matching opportunity: "${role}" at "${comp}". Clicking Easy Apply...`, "info");
    setTimeout(() => {
      addLog(`Auto-filled screening answers for "${role}" at "${comp}". Submitting!`, "success");
      recordJob(role, comp, AppState.activePlatform.toUpperCase(), 'Applied');
    }, 600);
  } else {
    addLog(`"${role}" at "${comp}" requires external employer ATS. Skipped per safety rules.`, "warning");
    recordJob(role, comp, AppState.activePlatform.toUpperCase(), 'Skipped', 'External ATS');
  }
}

// --- Injected Bookmarklet & Script Generator ---
function generateInjectedScript() {
  const profileJson = JSON.stringify(AppState.profile);
  const settingsJson = JSON.stringify(AppState.settings);

  return `(function() {
    if (window.__JobAutoApplyLoaded) {
      alert("JobAutoApply is already running on this tab!");
      return;
    }
    window.__JobAutoApplyLoaded = true;

    const profile = ${profileJson};
    const settings = ${settingsJson};

    // Create Floating HUD on Target Page
    const hud = document.createElement('div');
    hud.id = 'job-auto-apply-hud';
    hud.style.cssText = 'position:fixed;bottom:24px;right:24px;z-index:999999;background:#0F172A;color:#F9FAFB;padding:16px 20px;border-radius:14px;box-shadow:0 12px 36px rgba(0,0,0,0.6);border:1px solid #3B82F6;font-family:sans-serif;font-size:13px;min-width:280px;';
    hud.innerHTML = \`
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px;">
        <strong style="color:#3B82F6;font-size:14px;">⚡ JobAutoApply Active</strong>
        <button id="closeHudBtn" style="background:none;border:none;color:#9CA3AF;cursor:pointer;font-size:16px;">✕</button>
      </div>
      <div id="hudStatus" style="color:#10B981;font-weight:600;margin-bottom:6px;">Scanning feed...</div>
      <div style="font-size:11px;color:#9CA3AF;margin-bottom:12px;">Applied: <span id="hudCount" style="color:#fff;font-weight:700;">0</span> / \${settings.maxApplications}</div>
      <div style="display:flex;gap:8px;">
        <button id="hudPauseBtn" style="background:#F59E0B;border:none;color:#111;padding:6px 12px;border-radius:6px;font-weight:700;cursor:pointer;flex:1;">Pause</button>
        <button id="hudStopBtn" style="background:#F43F5E;border:none;color:#fff;padding:6px 12px;border-radius:6px;font-weight:700;cursor:pointer;">Stop</button>
      </div>
    \`;
    document.body.appendChild(hud);

    let isRunning = true;
    let appliedCount = 0;
    const isLinkedIn = window.location.hostname.includes('linkedin.com');

    document.getElementById('closeHudBtn').onclick = () => { isRunning = false; hud.remove(); };
    document.getElementById('hudStopBtn').onclick = () => { isRunning = false; hud.remove(); };
    document.getElementById('hudPauseBtn').onclick = function() {
      isRunning = !isRunning;
      this.innerText = isRunning ? 'Pause' : 'Resume';
      this.style.background = isRunning ? '#F59E0B' : '#10B981';
      document.getElementById('hudStatus').innerText = isRunning ? 'Scanning...' : 'Paused';
    };

    function log(msg, color='#10B981') {
      const el = document.getElementById('hudStatus');
      if (el) { el.innerText = msg; el.style.color = color; }
      console.log('[JobAutoApply] ' + msg);
    }

    async function cycle() {
      if (!isRunning) return;
      if (appliedCount >= settings.maxApplications) {
        log('Session limit reached!', '#3B82F6');
        return;
      }

      try {
        if (isLinkedIn) {
          await handleLinkedInStep();
        } else {
          await handleInstahyreStep();
        }
      } catch(e) {
        console.error(e);
      }

      const delay = Math.floor(Math.random() * ((settings.maxDelay - settings.minDelay) * 1000)) + (settings.minDelay * 1000);
      setTimeout(cycle, delay);
    }

    async function handleInstahyreStep() {
      const modal = document.querySelector('.candidate-apply-modal, .app-modal, #opportunity-modal');
      if (modal && modal.offsetParent !== null) {
        const applyBtn = Array.from(modal.querySelectorAll('button, a.btn')).find(b => (b.innerText||'').trim().toLowerCase() === 'apply');
        if (applyBtn) {
          applyBtn.click();
          appliedCount++;
          document.getElementById('hudCount').innerText = appliedCount;
          log('Applied to job on Instahyre!');
          return;
        }
      }

      const buttons = Array.from(document.querySelectorAll('button, a.btn'));
      const directApply = buttons.find(b => (b.innerText||'').trim().toLowerCase() === 'apply' && b.offsetParent !== null);
      if (directApply) {
        directApply.click();
        appliedCount++;
        document.getElementById('hudCount').innerText = appliedCount;
        log('Clicked Apply button!');
        return;
      }

      window.scrollBy({ top: 350, behavior: 'smooth' });
    }

    async function handleLinkedInStep() {
      const modal = document.querySelector('.jobs-easy-apply-modal, .artdeco-modal, div[role="dialog"]');
      if (modal && modal.offsetParent !== null) {
        // Auto fill phone
        const phone = modal.querySelector('input[type="tel"], input[id*="phoneNumber"]');
        if (phone && !phone.value) {
          phone.value = profile.phoneNumber;
          phone.dispatchEvent(new Event('input', {bubbles:true}));
        }

        // Auto select authorization
        const yesAuth = modal.querySelector('input[type="radio"][value="Yes"]');
        if (yesAuth && !yesAuth.checked) yesAuth.click();

        const submit = Array.from(modal.querySelectorAll('button')).find(b => (b.innerText||'').toLowerCase().includes('submit application'));
        if (submit) {
          submit.click();
          appliedCount++;
          document.getElementById('hudCount').innerText = appliedCount;
          log('Application submitted on LinkedIn!', '#10B981');
          return;
        }

        const next = Array.from(modal.querySelectorAll('button')).find(b => (b.innerText||'').toLowerCase().includes('next') || (b.innerText||'').toLowerCase().includes('review'));
        if (next) {
          next.click();
          log('Advancing step...');
          return;
        }
      }

      const applyButtons = Array.from(document.querySelectorAll('button.jobs-apply-button, button[aria-label*="Easy Apply"]'));
      const btn = applyButtons.find(b => b.offsetParent !== null);
      if (btn) {
        btn.click();
        log('Clicked Easy Apply...');
        return;
      }

      window.scrollBy({ top: 400, behavior: 'smooth' });
    }

    log('Bot started. Monitoring...');
    cycle();
  })();`;
}

function updateBookmarkletCode() {
  const rawCode = generateInjectedScript();
  const minified = "javascript:" + encodeURIComponent(rawCode.replace(/\n\s*/g, ' '));
  
  const link = document.getElementById('bookmarkletLink');
  if (link) {
    link.href = minified;
  }

  const preview = document.getElementById('scriptPreviewText');
  if (preview) {
    preview.value = rawCode;
  }
}

function copyBookmarkletScript(e) {
  e.preventDefault();
  const raw = generateInjectedScript();
  navigator.clipboard.writeText(raw).then(() => {
    alert("✓ Script copied to clipboard! You can paste it into the Developer Console (F12) on LinkedIn or Instahyre, or drag this button to your bookmarks bar.");
  });
}

function copyFullScript() {
  const raw = generateInjectedScript();
  navigator.clipboard.writeText(raw).then(() => {
    alert("✓ Full automation script copied to clipboard!");
  });
}
