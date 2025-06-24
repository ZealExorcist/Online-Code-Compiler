<template>  <div class="upgrade-component" :data-theme="userInfo?.settings?.theme || 'light'">
    <button @click="showUpgradeModal = true" class="upgrade-btn" v-if="userInfo">
      <i class="icon">⬆️</i>
      Upgrade Plan ({{ userInfo.tier }})
    </button>

    <!-- Upgrade Modal -->
    <div v-if="showUpgradeModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>Upgrade Your Plan</h3>
          <button @click="closeModal" class="close-btn">&times;</button>
        </div>
        
        <div class="modal-body">
          <div class="current-plan">
            <h4>Current Plan: {{ userInfo?.tier || 'BASIC' }}</h4>
            <p class="tier-description">{{ getCurrentTierDescription() }}</p>
          </div>

          <div class="plans-grid">
            <div 
              v-for="(plan, tierName) in availableTiers" 
              :key="tierName"
              class="plan-card"
              :class="{ 
                'current': userInfo?.tier === tierName,
                'selected': selectedTier === tierName 
              }"
              @click="selectTier(tierName)"
            >
              <div class="plan-header">
                <h4>{{ tierName }}</h4>
                <div class="plan-badge" v-if="userInfo?.tier === tierName">Current</div>
              </div>
              
              <div class="plan-details">
                <p class="description">{{ plan.description }}</p>
                <div class="features">
                  <div class="feature">
                    <span class="feature-label">Requests per hour:</span>
                    <span class="feature-value">{{ plan.requestsPerHour }}</span>
                  </div>
                  <div class="feature">
                    <span class="feature-label">Compilation delay:</span>
                    <span class="feature-value">
                      {{ plan.delayMs === 0 ? 'Instant' : `${plan.delayMs / 1000}s` }}
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>          <div class="upgrade-actions" v-if="selectedTier && selectedTier !== userInfo?.tier">
            <button 
              @click="showUpgradeConfirmation" 
              :disabled="isUpgrading"
              class="upgrade-action-btn"
            >
              {{ isUpgrading ? 'Upgrading...' : `Upgrade to ${selectedTier}` }}
            </button>
          </div>
        </div>      </div>
    </div>

    <!-- Upgrade Success Modal -->
    <div v-if="showUpgradeSuccessModal" class="modal-overlay" @click="closeUpgradeSuccessModal">
      <div class="modal-content upgrade-success-modal" @click.stop>
        <div class="modal-header">
          <h3>🎉 Plan Upgraded Successfully!</h3>
          <button @click="closeUpgradeSuccessModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="success-info">
            <div class="success-icon">✅</div>
            <h4>Welcome to {{ userInfo?.tier }}!</h4>
            <p>Your plan has been successfully upgraded. You now have access to enhanced features:</p>
            <div class="new-features">
              <div class="feature" v-if="userInfo?.tier === 'ADVANCED' || userInfo?.tier === 'MASTER'">
                <span class="feature-icon">📥</span>
                <span class="feature-text">Download code to local machine</span>
              </div>
              <div class="feature" v-if="userInfo?.tier === 'MASTER'">
                <span class="feature-icon">⚡</span>
                <span class="feature-text">Custom command execution</span>
              </div>
              <div class="feature">
                <span class="feature-icon">🚀</span>
                <span class="feature-text">Increased rate limits and faster execution</span>
              </div>
            </div>
            <p class="reload-notice">The page will reload automatically to activate your new features.</p>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="proceedWithReload" class="btn-primary">Continue</button>
        </div>      </div>
    </div>

    <!-- Upgrade Confirmation Modal -->
    <div v-if="showConfirmationModal" class="modal-overlay" @click="closeConfirmationModal">
      <div class="modal-content confirmation-modal" @click.stop>
        <div class="modal-header">
          <h3>⚠️ Confirm Plan Upgrade</h3>
          <button @click="closeConfirmationModal" class="close-btn">&times;</button>
        </div>
        <div class="modal-body">
          <div class="confirmation-info">
            <div class="confirmation-icon">💳</div>
            <h4>Upgrade to {{ selectedTier }}?</h4>
            <div class="plan-comparison">
              <div class="current-plan-info">
                <span class="plan-label">Current Plan:</span>
                <span class="plan-name current">{{ userInfo?.tier || 'BASIC' }}</span>
              </div>
              <div class="upgrade-arrow">→</div>
              <div class="new-plan-info">
                <span class="plan-label">New Plan:</span>
                <span class="plan-name new">{{ selectedTier }}</span>
              </div>
            </div>
            <div class="upgrade-benefits-preview">
              <p><strong>You'll get access to:</strong></p>
              <ul class="benefits-preview-list">
                <li v-if="selectedTier === 'ADVANCED' || selectedTier === 'MASTER'">📥 Download code files to local machine</li>
                <li v-if="selectedTier === 'MASTER'">⚡ Custom package installation commands</li>
                <li>🚀 {{ availableTiers[selectedTier]?.requestsPerHour || 'Unlimited' }} requests per hour</li>
                <li>⚡ {{ availableTiers[selectedTier]?.delayMs === 0 ? 'Instant' : `${availableTiers[selectedTier]?.delayMs / 1000}s` }} execution delay</li>
              </ul>
            </div>
            <p class="confirmation-note">This change will take effect immediately.</p>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeConfirmationModal" class="btn-secondary">Cancel</button>
          <button @click="confirmUpgrade" class="btn-primary" :disabled="isUpgrading">
            <span v-if="isUpgrading">⏳ Upgrading...</span>
            <span v-else>✅ Confirm Upgrade</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Success Message -->
    <div v-if="upgradeSuccess" class="success-message">
      <p>✅ Successfully upgraded to {{ selectedTier }}!</p>
      <p class="reload-notice">🔄 Reloading page to update features...</p>
    </div>

    <!-- Error Message -->
    <div v-if="upgradeError" class="error-message">
      <p>❌ {{ upgradeError }}</p>
    </div>
  </div>
</template>

<script>
import { getUserProfile, getAvailableTiers, upgradeTier } from '../services/settings'
import { eventBus } from '../services/eventBus'

export default {
  name: 'UpgradeComponent',  data() {
    return {
      showUpgradeModal: false,
      showUpgradeSuccessModal: false,
      showConfirmationModal: false,
      userInfo: null,
      availableTiers: {},
      selectedTier: null,
      isUpgrading: false,
      upgradeSuccess: false,
      upgradeError: null
    }
  },
  async mounted() {
    await this.loadUserInfo()
    await this.loadTiers()
  },
  methods: {
    async loadUserInfo() {
      try {
        this.userInfo = await getUserProfile()
      } catch (error) {
        console.error('Failed to load user info:', error)
      }
    },

    async loadTiers() {
      try {
        this.availableTiers = await getAvailableTiers()
      } catch (error) {
        console.error('Failed to load tiers:', error)
      }
    },

    getCurrentTierDescription() {
      if (!this.userInfo?.tier || !this.availableTiers[this.userInfo.tier]) {
        return 'Basic plan with standard features'
      }
      return this.availableTiers[this.userInfo.tier].description
    },

    selectTier(tierName) {
      if (tierName !== this.userInfo?.tier) {
        this.selectedTier = tierName
      }    },

    async upgradePlan() {
      if (!this.selectedTier) return

      this.isUpgrading = true
      this.upgradeError = null
      this.upgradeSuccess = false

      try {
        await upgradeTier(this.selectedTier)
        this.upgradeSuccess = true
        this.userInfo.tier = this.selectedTier
        
        // Update global state via event bus
        eventBus.updateUserTier(this.selectedTier)
        
        // Emit event to update parent components
        this.$emit('tier-updated', this.selectedTier)
        
        // Show success modal
        this.showUpgradeSuccessModal = true
        
        // Close upgrade modal
        this.showUpgradeModal = false
        
      } catch (error) {
        this.upgradeError = error.message || 'Failed to upgrade plan'
        console.error('Upgrade failed:', error)
      } finally {
        this.isUpgrading = false
      }
    },    closeModal() {
      this.showUpgradeModal = false
      this.selectedTier = null
      this.upgradeError = null
      this.upgradeSuccess = false
    },

    closeUpgradeSuccessModal() {
      this.showUpgradeSuccessModal = false
    },    proceedWithReload() {
      this.closeUpgradeSuccessModal()
      // Small delay to let the modal close gracefully
      setTimeout(() => {
        window.location.reload()
      }, 300)
    },

    showUpgradeConfirmation() {
      this.showConfirmationModal = true
    },

    closeConfirmationModal() {
      this.showConfirmationModal = false
    },

    async confirmUpgrade() {
      // Close confirmation modal and proceed with upgrade
      this.closeConfirmationModal()
      await this.upgradePlan()
    }
  }
}
</script>

<style scoped>
.upgrade-component {
  position: relative;
}

.upgrade-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
}

.upgrade-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: var(--modal-bg, white);
  border-radius: 12px;
  padding: 0;
  max-width: 800px;
  width: 90vw;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
  display: flex;
  flex-direction: column;
  color: var(--text-color, #1f2937);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid var(--border-color, #e5e7eb);
  background: var(--header-bg, #f9fafb);
}

.modal-header h3 {
  margin: 0;
  color: var(--text-color, #1f2937);
  font-size: 20px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.2s;
}

.close-btn:hover {
  background: #e5e7eb;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.current-plan {
  margin-bottom: 24px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.current-plan h4 {
  margin: 0 0 8px 0;
  color: #1f2937;
}

.tier-description {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.plan-card {
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  padding: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.plan-card:hover {
  border-color: #667eea;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.1);
}

.plan-card.current {
  border-color: #10b981;
  background: #f0fdf4;
}

.plan-card.selected {
  border-color: #667eea;
  background: #f0f4ff;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.plan-header h4 {
  margin: 0;
  color: #1f2937;
  font-size: 18px;
}

.plan-badge {
  background: #10b981;
  color: white;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.description {
  margin: 0 0 16px 0;
  color: #6b7280;
  font-size: 14px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.feature-label {
  color: #6b7280;
  font-size: 14px;
}

.feature-value {
  color: #1f2937;
  font-weight: 500;
  font-size: 14px;
}

.upgrade-actions {
  text-align: center;
}

.upgrade-action-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.upgrade-action-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.upgrade-action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Modal Button Styles */
.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 140px;
  justify-content: center;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn-secondary {
  background: transparent;
  color: #6b7280;
  border: 2px solid #e5e7eb;
  padding: 12px 24px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 100px;
}

.btn-secondary:hover {
  background: #f9fafb;
  border-color: #d1d5db;
  color: #374151;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.success-message,
.error-message {
  position: absolute;
  bottom: 20px;
  right: 20px;
  padding: 12px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  z-index: 1001;
}

.success-message {
  background: #10b981;
  color: white;
}

.reload-notice {
  margin-top: 5px;
  font-size: 12px;
  opacity: 0.9;
  font-style: italic;
}

.error-message {
  background: #ef4444;
  color: white;
}

/* Dark mode support */
.upgrade-component[data-theme="dark"] .modal-content {
  --modal-bg: #1f2937;
  --text-color: #f9fafb;
  --header-bg: #374151;
  --border-color: #4b5563;
}

.upgrade-component[data-theme="dark"] .current-plan {
  background: #374151;
  border-color: #4b5563;
}

.upgrade-component[data-theme="dark"] .current-plan h4 {
  color: #f9fafb;
}

.upgrade-component[data-theme="dark"] .tier-description {
  color: #d1d5db;
}

.upgrade-component[data-theme="dark"] .plan-card {
  background: #374151;
  border-color: #4b5563;
}

.upgrade-component[data-theme="dark"] .plan-card:hover {
  border-color: #667eea;
}

.upgrade-component[data-theme="dark"] .plan-card.current {
  background: #065f46;
  border-color: #10b981;
}

.upgrade-component[data-theme="dark"] .plan-card.selected {
  background: #1e3a8a;
  border-color: #667eea;
}

.upgrade-component[data-theme="dark"] .plan-header h4 {
  color: #f9fafb;
}

.upgrade-component[data-theme="dark"] .description {
  color: #d1d5db;
}

.upgrade-component[data-theme="dark"] .feature-label {
  color: #9ca3af;
}

.upgrade-component[data-theme="dark"] .feature-value {
  color: #f9fafb;
}

.upgrade-component[data-theme="dark"] .close-btn:hover {
  background: #4b5563;
}

/* Upgrade Success Modal */
.upgrade-success-modal {
  max-width: 500px;
}

.success-info {
  text-align: center;
}

.success-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.success-info h4 {
  color: #059669;
  margin: 0 0 1rem 0;
  font-size: 1.5rem;
}

.success-info p {
  margin: 0.5rem 0;
  color: #6b7280;
}

.new-features {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  padding: 1rem;
  margin: 1rem 0;
  text-align: left;
}

.feature {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0.5rem 0;
}

.feature-icon {
  font-size: 1.2rem;
}

.feature-text {
  color: #059669;
  font-weight: 500;
}

.reload-notice {
  color: #6b7280;
  font-size: 0.9rem;
  font-style: italic;
  margin-top: 1rem;
}

/* Dark mode for success modal */
.upgrade-component[data-theme="dark"] .success-info h4 {
  color: #10b981;
}

.upgrade-component[data-theme="dark"] .success-info p {
  color: #d1d5db;
}

.upgrade-component[data-theme="dark"] .new-features {
  background: #064e3b;
  border-color: #065f46;
}

.upgrade-component[data-theme="dark"] .feature-text {
  color: #10b981;
}

.upgrade-component[data-theme="dark"] .reload-notice {
  color: #9ca3af;
}

/* Confirmation Modal Styles */
.confirmation-modal {
  max-width: 500px;
}

.confirmation-info {
  text-align: center;
}

.confirmation-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.confirmation-info h4 {
  color: #f59e0b;
  margin: 0 0 1.5rem 0;
  font-size: 1.5rem;
}

.plan-comparison {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  margin: 1rem 0;
}

.current-plan-info,
.new-plan-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.plan-label {
  font-size: 0.8rem;
  color: #6b7280;
  text-transform: uppercase;
  font-weight: 500;
}

.plan-name {
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 600;
  text-transform: uppercase;
}

.plan-name.current {
  background: #dbeafe;
  color: #1e40af;
}

.plan-name.new {
  background: #d1fae5;
  color: #065f46;
}

.upgrade-arrow {
  font-size: 1.5rem;
  color: #059669;
  font-weight: bold;
}

.upgrade-benefits-preview {
  background: #f0fdf4;
  border: 1px solid #bbf7d0;
  border-radius: 8px;
  padding: 1rem;
  margin: 1rem 0;
  text-align: left;
}

.upgrade-benefits-preview p {
  margin: 0 0 0.5rem 0;
  color: #065f46;
  font-weight: 600;
}

.benefits-preview-list {
  margin: 0;
  padding-left: 1rem;
}

.benefits-preview-list li {
  margin: 0.5rem 0;
  color: #059669;
  font-size: 0.9rem;
}

.confirmation-note {
  color: #6b7280;
  font-size: 0.9rem;
  font-style: italic;
  margin: 1rem 0 0 0;
}

/* Dark mode for confirmation modal */
.upgrade-component[data-theme="dark"] .confirmation-info h4 {
  color: #fbbf24;
}

.upgrade-component[data-theme="dark"] .plan-comparison {
  background: #374151;
  border-color: #4b5563;
}

.upgrade-component[data-theme="dark"] .plan-label {
  color: #9ca3af;
}

.upgrade-component[data-theme="dark"] .plan-name.current {
  background: #1e3a8a;
  color: #93c5fd;
}

.upgrade-component[data-theme="dark"] .plan-name.new {
  background: #065f46;
  color: #6ee7b7;
}

.upgrade-component[data-theme="dark"] .upgrade-benefits-preview {
  background: #064e3b;
  border-color: #065f46;
}

.upgrade-component[data-theme="dark"] .upgrade-benefits-preview p {
  color: #6ee7b7;
}

.upgrade-component[data-theme="dark"] .benefits-preview-list li {
  color: #10b981;
}

.upgrade-component[data-theme="dark"] .confirmation-note {
  color: #9ca3af;
}

/* Dark mode for buttons */
.upgrade-component[data-theme="dark"] .btn-secondary {
  color: #d1d5db;
  border-color: #4b5563;
}

.upgrade-component[data-theme="dark"] .btn-secondary:hover {
  background: #4b5563;
  border-color: #6b7280;
  color: #f9fafb;
}
</style>
