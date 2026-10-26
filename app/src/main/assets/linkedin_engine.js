/**
 * LinkedIn Easy Apply Mobile Automation Engine for Android JobAutoApply
 * Injected into WebView for autonomous job applications
 */
(function() {
    if (window.__LinkedInEngineLoaded) {
        console.log("[LinkedInEngine] Already loaded.");
        return;
    }
    window.__LinkedInEngineLoaded = true;

    const Engine = {
        isRunning: false,
        isPaused: false,
        settings: {
            minDelay: 3.0,
            maxDelay: 5.5,
            maxApplications: 30,
            autoAnswerQuestions: true
        },
        profile: {
            fullName: "Job Seeker",
            phoneNumber: "9876543210",
            totalExperience: 3,
            noticePeriodDays: 30,
            currentCtcLakhs: 12,
            expectedCtcLakhs: 18,
            isAuthorizedToWork: true,
            requiresSponsorship: false
        },
        appliedCount: 0,
        skippedCount: 0,
        processedJobIds: new Set(),
        timerId: null,

        log(msg, level = "info") {
            console.log(`[LinkedInEngine] [${level.toUpperCase()}] ${msg}`);
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
                if (profileJson) this.profile = Object.assign(this.profile, JSON.parse(profileJson));
                this.log("LinkedIn Engine initialized.");
            } catch (e) {
                this.log("Error initializing config: " + e.message, "error");
            }
        },

        start() {
            if (this.isRunning && !this.isPaused) return;
            this.isRunning = true;
            this.isPaused = false;
            this.updateStatus("RUNNING", "Starting LinkedIn Easy Apply cycle...");
            this.log("LinkedIn Auto-Apply started.");
            this.loop();
        },

        pause() {
            this.isPaused = true;
            this.updateStatus("PAUSED", "Automation paused by user");
            this.log("LinkedIn Auto-Apply paused.");
            if (this.timerId) clearTimeout(this.timerId);
        },

        resume() {
            if (!this.isRunning) {
                this.start();
                return;
            }
            this.isPaused = false;
            this.updateStatus("RUNNING", "Resuming LinkedIn cycle...");
            this.log("LinkedIn Auto-Apply resumed.");
            this.loop();
        },

        stop() {
            this.isRunning = false;
            this.isPaused = false;
            if (this.timerId) clearTimeout(this.timerId);
            this.updateStatus("IDLE", "Stopped");
            this.log("LinkedIn Auto-Apply stopped.");
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

        async loop() {
            if (!this.isRunning || this.isPaused) return;

            if (this.appliedCount >= this.settings.maxApplications) {
                this.log(`Reached LinkedIn limit of ${this.settings.maxApplications} applications.`, "success");
                this.updateStatus("COMPLETED", "Target completed");
                this.stop();
                return;
            }

            try {
                await this.processNextJob();
                const delay = this.getRandomDelay();
                this.updateStatus("WAITING", `Pacing next application in ${(delay / 1000).toFixed(1)}s...`);
                this.timerId = setTimeout(() => this.loop(), delay);
            } catch (err) {
                this.log("Cycle error: " + err.message, "error");
                this.timerId = setTimeout(() => this.loop(), 5000);
            }
        },

        async processNextJob() {
            // Check if modal dialog is currently open
            const modal = document.querySelector('.jobs-easy-apply-modal, .artdeco-modal, div[role="dialog"]');
            if (modal && this.isVisible(modal)) {
                await this.handleApplicationModal(modal);
                return;
            }

            // Find all job items in current list
            const jobCards = Array.from(document.querySelectorAll(
                'li.jobs-search-results__list-item, div.job-card-container, div[data-job-id], li[data-occludable-job-id]'
            ));

            if (jobCards.length === 0) {
                this.log("No job cards found. Ensure you are on a LinkedIn Jobs search results page.", "warning");
                window.scrollBy({ top: 300, behavior: 'smooth' });
                return;
            }

            // Find an unprocessed job card
            let targetCard = null;
            let targetJobId = null;

            for (const card of jobCards) {
                const jobId = card.getAttribute('data-job-id') || 
                              card.getAttribute('data-occludable-job-id') || 
                              card.innerText.slice(0, 30);
                if (!this.processedJobIds.has(jobId)) {
                    targetCard = card;
                    targetJobId = jobId;
                    break;
                }
            }

            if (!targetCard) {
                this.log("Processed all visible jobs. Scrolling down for more...");
                window.scrollBy({ top: 600, behavior: 'smooth' });
                return;
            }

            this.processedJobIds.add(targetJobId);

            // Extract title and company
            const titleEl = targetCard.querySelector('.job-card-list__title, .artdeco-entity-lockup__title, strong, h3');
            const companyEl = targetCard.querySelector('.job-card-container__primary-description, .artdeco-entity-lockup__subtitle, h4');
            const title = titleEl ? titleEl.innerText.trim() : "Job Opportunity";
            const company = companyEl ? companyEl.innerText.trim() : "Company";

            this.log(`Inspecting: "${title}" at "${company}"`);
            targetCard.scrollIntoView({ behavior: 'smooth', block: 'center' });
            await new Promise(r => setTimeout(r, 600));

            // Click the card to open details pane
            const clickable = targetCard.querySelector('a') || targetCard;
            clickable.click();
            await new Promise(r => setTimeout(r, 1200));

            // Check if Easy Apply is available
            const applyButtons = Array.from(document.querySelectorAll(
                'button.jobs-apply-button, button[aria-label*="Easy Apply"], button.artdeco-button--primary'
            ));

            const easyApplyBtn = applyButtons.find(b => {
                const text = (b.innerText || '').toLowerCase();
                const aria = (b.getAttribute('aria-label') || '').toLowerCase();
                return (text.includes('easy apply') || aria.includes('easy apply')) && this.isVisible(b);
            });

            if (!easyApplyBtn) {
                this.log(`"${title}" is not an Easy Apply job (requires external site). Skipping.`, "warning");
                if (window.AndroidBridge && window.AndroidBridge.onJobSkipped) {
                    window.AndroidBridge.onJobSkipped(title, company, "Not Easy Apply", "linkedin");
                }
                this.skippedCount++;
                return;
            }

            // Click Easy Apply
            this.log(`Clicking Easy Apply for "${title}"...`);
            easyApplyBtn.click();
            await new Promise(r => setTimeout(r, 1500));

            const newModal = document.querySelector('.jobs-easy-apply-modal, .artdeco-modal, div[role="dialog"]');
            if (newModal) {
                await this.handleApplicationModal(newModal, title, company, targetJobId);
            }
        },

        async handleApplicationModal(modal, title = "Position", company = "Company", jobId = "job") {
            let steps = 0;
            const maxSteps = 8;

            while (steps < maxSteps && this.isRunning && !this.isPaused) {
                steps++;
                await new Promise(r => setTimeout(r, 800));

                // Auto-fill questions on current step
                this.fillCurrentFormStep(modal);

                // Look for Next / Review / Submit buttons
                const buttons = Array.from(modal.querySelectorAll('button'));
                
                const submitBtn = buttons.find(b => {
                    const text = (b.innerText || '').toLowerCase();
                    const aria = (b.getAttribute('aria-label') || '').toLowerCase();
                    return (text.includes('submit application') || aria.includes('submit application')) && this.isVisible(b);
                });

                if (submitBtn) {
                    this.log(`Submitting application for "${title}" at "${company}"!`, "success");
                    submitBtn.click();
                    this.appliedCount++;
                    if (window.AndroidBridge && window.AndroidBridge.onJobApplied) {
                        window.AndroidBridge.onJobApplied(title, company, jobId, "linkedin");
                    }
                    await new Promise(r => setTimeout(r, 1800));
                    this.dismissPostApplyModal();
                    return;
                }

                const reviewBtn = buttons.find(b => {
                    const text = (b.innerText || '').toLowerCase();
                    const aria = (b.getAttribute('aria-label') || '').toLowerCase();
                    return (text.includes('review') || aria.includes('review')) && this.isVisible(b);
                });

                if (reviewBtn) {
                    this.log("Stepping to review screen...");
                    reviewBtn.click();
                    continue;
                }

                const nextBtn = buttons.find(b => {
                    const text = (b.innerText || '').toLowerCase();
                    const aria = (b.getAttribute('aria-label') || '').toLowerCase();
                    return (text.includes('next') || aria.includes('next') || aria.includes('continue')) && this.isVisible(b);
                });

                if (nextBtn) {
                    this.log(`Advancing to step ${steps + 1}...`);
                    nextBtn.click();
                    continue;
                }

                // If no navigation button was found, check if completed
                break;
            }

            // Dismiss modal if still open
            this.dismissPostApplyModal();
        },

        fillCurrentFormStep(modal) {
            // Fill phone numbers if input empty
            const phoneInputs = modal.querySelectorAll('input[type="tel"], input[id*="phoneNumber"], input[name*="phoneNumber"]');
            phoneInputs.forEach(input => {
                if (!input.value && this.profile.phoneNumber) {
                    input.value = this.profile.phoneNumber;
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                    input.dispatchEvent(new Event('change', { bubbles: true }));
                }
            });

            // Handle common screening questions
            const formRows = modal.querySelectorAll('.jobs-easy-apply-form-section__grouping, .fb-dash-form-element');
            formRows.forEach(row => {
                const label = (row.innerText || '').toLowerCase();

                // Work Authorization
                if (label.includes('authorized') || label.includes('legally')) {
                    const yesRadio = row.querySelector('input[type="radio"][value="Yes"], input[type="radio"][id*="yes"]');
                    if (yesRadio && !yesRadio.checked) yesRadio.click();
                }

                // Visa Sponsorship
                if (label.includes('sponsorship') || label.includes('require visa')) {
                    const noRadio = row.querySelector('input[type="radio"][value="No"], input[type="radio"][id*="no"]');
                    if (noRadio && !noRadio.checked) noRadio.click();
                }

                // Experience questions (e.g. "How many years of experience...")
                if (label.includes('how many years') || label.includes('experience')) {
                    const numInput = row.querySelector('input[type="text"], input[type="number"]');
                    if (numInput && !numInput.value) {
                        numInput.value = this.profile.totalExperience || "3";
                        numInput.dispatchEvent(new Event('input', { bubbles: true }));
                        numInput.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                }

                // Notice period
                if (label.includes('notice period')) {
                    const numInput = row.querySelector('input[type="text"], input[type="number"]');
                    if (numInput && !numInput.value) {
                        numInput.value = this.profile.noticePeriodDays || "30";
                        numInput.dispatchEvent(new Event('input', { bubbles: true }));
                        numInput.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                }
            });
        },

        dismissPostApplyModal() {
            const dismissBtn = document.querySelector(
                'button[aria-label="Dismiss"], button[aria-label="Done"], button[data-control-name="overlay.close_padding"]'
            );
            if (dismissBtn) dismissBtn.click();
        }
    };

    window.__LinkedInEngine = Engine;
    console.log("[LinkedInEngine] Engine ready.");
})();
