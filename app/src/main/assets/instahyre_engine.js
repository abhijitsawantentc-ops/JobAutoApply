/**
 * InstaHyre Mobile Automation Engine for Android JobAutoApply
 * Injected into WebView for autonomous job applications
 */
(function() {
    if (window.__InstaHyreEngineLoaded) {
        console.log("[InstaHyreEngine] Already loaded.");
        return;
    }
    window.__InstaHyreEngineLoaded = true;

    const Engine = {
        isRunning: false,
        isPaused: false,
        settings: {
            minDelay: 2.5,
            maxDelay: 4.5,
            maxApplications: 50,
            skipLowMatch: false,
            autoBulkApply: true,
            skipExternal: true
        },
        profile: {},
        appliedCount: 0,
        skippedCount: 0,
        seenJobs: new Set(),
        timerId: null,

        log(msg, level = "info") {
            console.log(`[InstaHyreEngine] [${level.toUpperCase()}] ${msg}`);
            if (window.AndroidBridge && window.AndroidBridge.onLog) {
                window.AndroidBridge.onLog(msg, level);
            }
        },

        updateStatus(status, details = "") {
            if (window.AndroidBridge && window.AndroidBridge.onStatusUpdate) {
                window.AndroidBridge.onStatusUpdate(status, details);
            }
        },

        init(configJson, profileJson) {
            try {
                if (configJson) this.settings = Object.assign(this.settings, JSON.parse(configJson));
                if (profileJson) this.profile = JSON.parse(profileJson);
                this.log("Engine initialized with configuration.");
            } catch (e) {
                this.log("Error initializing engine config: " + e.message, "error");
            }
        },

        start() {
            if (this.isRunning && !this.isPaused) return;
            this.isRunning = true;
            this.isPaused = false;
            this.updateStatus("RUNNING", "Scanning for opportunities...");
            this.log("Instahyre Auto-Apply started.");
            this.loop();
        },

        pause() {
            this.isPaused = true;
            this.updateStatus("PAUSED", "Automation paused by user");
            this.log("Instahyre Auto-Apply paused.");
            if (this.timerId) clearTimeout(this.timerId);
        },

        resume() {
            if (!this.isRunning) {
                this.start();
                return;
            }
            this.isPaused = false;
            this.updateStatus("RUNNING", "Resuming automation...");
            this.log("Instahyre Auto-Apply resumed.");
            this.loop();
        },

        stop() {
            this.isRunning = false;
            this.isPaused = false;
            if (this.timerId) clearTimeout(this.timerId);
            this.updateStatus("IDLE", "Stopped");
            this.log("Instahyre Auto-Apply stopped.");
        },

        getRandomDelay() {
            const min = this.settings.minDelay * 1000;
            const max = this.settings.maxDelay * 1000;
            return Math.floor(Math.random() * (max - min + 1)) + min;
        },

        isVisible(el) {
            if (!el) return false;
            const style = window.getComputedStyle(el);
            if (style.display === 'none' || style.visibility === 'hidden' || style.opacity === '0') return false;
            const rect = el.getBoundingClientRect();
            return rect.width > 0 && rect.height > 0;
        },

        getActiveModal() {
            const modalSelectors = [
                '.candidate-apply-modal',
                '.app-modal',
                '.application-modal-wrap',
                '#opportunity-modal',
                '.modal.fade.in',
                '.modal-dialog'
            ];
            for (const sel of modalSelectors) {
                const els = document.querySelectorAll(sel);
                for (const el of els) {
                    if (!el.closest('.ng-hide') && this.isVisible(el)) {
                        return el;
                    }
                }
            }
            return null;
        },

        async loop() {
            if (!this.isRunning || this.isPaused) return;

            if (this.appliedCount >= this.settings.maxApplications) {
                this.log(`Reached target application limit of ${this.settings.maxApplications}.`, "success");
                this.updateStatus("COMPLETED", "Application limit reached");
                this.stop();
                return;
            }

            try {
                const handled = await this.processStep();
                const nextDelay = this.getRandomDelay();
                this.updateStatus("WAITING", `Waiting ${(nextDelay / 1000).toFixed(1)}s for next action...`);
                this.timerId = setTimeout(() => this.loop(), nextDelay);
            } catch (err) {
                this.log(`Error during cycle: ${err.message}`, "error");
                this.timerId = setTimeout(() => this.loop(), 4000);
            }
        },

        async processStep() {
            // Check 1: Is a modal open right now?
            const modal = this.getActiveModal();
            if (modal) {
                return await this.handleModal(modal);
            }

            // Check 2: Look for job action buttons on the current page
            // Instahyre has 'View job' or 'Apply' buttons on candidate feed
            const buttons = Array.from(document.querySelectorAll('button, a.btn, div.btn'));
            
            // Prioritize direct 'Apply' buttons
            const directApply = buttons.find(b => {
                const text = (b.innerText || '').trim().toLowerCase();
                return (text === 'apply' || text === 'quick apply') && 
                       this.isVisible(b) && 
                       !b.disabled && 
                       !b.classList.contains('ng-hide');
            });

            if (directApply) {
                const card = directApply.closest('.opportunity-card, .job-item, .card, div[ng-repeat]') || directApply.parentElement;
                const title = card ? (card.querySelector('.job-title, h4, h3, .designation')?.innerText || 'Opportunity') : 'Opportunity';
                const company = card ? (card.querySelector('.company-name, .employer, h5')?.innerText || 'Company') : 'Company';

                this.log(`Found direct apply button for "${title}" at "${company}". Clicking...`);
                directApply.scrollIntoView({ behavior: 'smooth', block: 'center' });
                await new Promise(r => setTimeout(r, 600));
                directApply.click();
                return true;
            }

            // Check for 'View job' buttons to open details
            const viewJobBtn = buttons.find(b => {
                const text = (b.innerText || '').trim().toLowerCase();
                return (text === 'view' || text === 'view job' || text === 'view & apply') && 
                       this.isVisible(b) && 
                       !b.disabled && 
                       !b.classList.contains('ng-hide');
            });

            if (viewJobBtn) {
                this.log("Clicking 'View job' to check details...");
                viewJobBtn.scrollIntoView({ behavior: 'smooth', block: 'center' });
                await new Promise(r => setTimeout(r, 500));
                viewJobBtn.click();
                return true;
            }

            // If nothing is visible, gently scroll down to load next opportunities
            this.log("Scrolling page to trigger infinite scroll...");
            window.scrollBy({ top: 400, behavior: 'smooth' });
            return false;
        },

        async handleModal(modal) {
            this.log("Examining open application modal...");

            // Check if already applied
            const appliedIndicator = modal.querySelector('.applied, .btn-applied, .already-applied');
            if (appliedIndicator || (modal.innerText && modal.innerText.includes('You have applied'))) {
                this.log("Job already applied. Closing modal...");
                this.closeModal(modal);
                return true;
            }

            // Extract job title and company from modal
            const title = modal.querySelector('.modal-title, .job-title, h3, h4')?.innerText?.trim() || 'Software Engineer';
            const company = modal.querySelector('.company-name, .employer, h5')?.innerText?.trim() || 'Company';

            // Check InstaMatch if enabled
            if (this.settings.skipLowMatch) {
                const matchBadge = modal.querySelector('.instamatch-badge, .match-score, .label-danger');
                if (matchBadge && matchBadge.innerText.toLowerCase().includes('low')) {
                    this.log(`Skipping "${title}" due to Low InstaMatch.`, "warning");
                    if (window.AndroidBridge && window.AndroidBridge.onJobSkipped) {
                        window.AndroidBridge.onJobSkipped(title, company, "Low InstaMatch", "instahyre");
                    }
                    this.closeModal(modal);
                    return true;
                }
            }

            // Check for external redirect
            const externalNotice = modal.querySelector('.external-apply, a[target="_blank"]');
            if (this.settings.skipExternal && externalNotice && externalNotice.innerText.toLowerCase().includes('employer website')) {
                this.log(`Skipping "${title}" because it redirects to external website.`, "warning");
                if (window.AndroidBridge && window.AndroidBridge.onJobSkipped) {
                    window.AndroidBridge.onJobSkipped(title, company, "External Redirect", "instahyre");
                }
                this.closeModal(modal);
                return true;
            }

            // Look for the primary "Apply" button inside the modal
            const modalButtons = Array.from(modal.querySelectorAll('button, a.btn'));
            const applyBtn = modalButtons.find(b => {
                const text = (b.innerText || '').trim().toLowerCase();
                return (text === 'apply' || text.includes('apply for job')) && 
                       this.isVisible(b) && 
                       !b.disabled;
            });

            if (applyBtn) {
                this.log(`Applying to: "${title}" at "${company}"!`, "success");
                applyBtn.click();
                this.appliedCount++;

                if (window.AndroidBridge && window.AndroidBridge.onJobApplied) {
                    window.AndroidBridge.onJobApplied(title, company, "insta_" + Date.now(), "instahyre");
                }

                // Wait a moment for confirmation or 'similar jobs' popup
                await new Promise(r => setTimeout(r, 1500));
                this.handlePostApplyPopup();
                return true;
            }

            // If no apply button found, dismiss
            this.closeModal(modal);
            return false;
        },

        handlePostApplyPopup() {
            // Check for 'similar jobs' or bulk apply checkboxes
            if (this.settings.autoBulkApply) {
                const checkboxes = document.querySelectorAll('.similar-jobs input[type="checkbox"], .bulk-apply input[type="checkbox"]');
                checkboxes.forEach(cb => {
                    if (!cb.checked) cb.click();
                });
                
                const bulkApplyBtn = Array.from(document.querySelectorAll('button, a.btn')).find(b => {
                    const text = (b.innerText || '').trim().toLowerCase();
                    return text.includes('apply to selected') || text.includes('apply to all');
                });
                if (bulkApplyBtn) {
                    this.log("Auto-applying to selected similar opportunities!");
                    bulkApplyBtn.click();
                }
            }
        },

        closeModal(modal) {
            const closeBtn = (modal || document).querySelector('.close, .btn-close, button[data-dismiss="modal"], button.close-modal');
            if (closeBtn) {
                closeBtn.click();
            } else {
                // Press Escape key as fallback
                document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', code: 'Escape', bubbles: true }));
            }
        }
    };

    window.__InstaHyreEngine = Engine;
    console.log("[InstaHyreEngine] Engine ready.");
})();
