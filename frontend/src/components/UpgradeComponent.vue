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
          </div>

          <div class="upgrade-actions" v-if="selectedTier && selectedTier !== userInfo?.tier">
            <button 
              @click="upgradePlan" 
              :disabled="isUpgrading"
              class="upgrade-action-btn"
            >
              {{ isUpgrading ? 'Upgrading...' : `Upgrade to ${selectedTier}` }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Success Message -->
    <div v-if="upgradeSuccess" class="success-message">
      <p>✅ Successfully upgraded to {{ selectedTier }}!</p>
    </div>

    <!-- Error Message -->
    <div v-if="upgradeError" class="error-message">
      <p>❌ {{ upgradeError }}</p>
    </div>
  </div>
</template>

<script>
import { getUserProfile, getAvailableTiers, upgradeTier } from '../services/settings'

export default {
  name: 'UpgradeComponent',
  data() {
    return {
      showUpgradeModal: false,
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
      }
    },

    async upgradePlan() {
      if (!this.selectedTier) return

      this.isUpgrading = true
      this.upgradeError = null
      this.upgradeSuccess = false

      try {
        await upgradeTier(this.selectedTier)
        this.upgradeSuccess = true
        this.userInfo.tier = this.selectedTier
        
        // Auto-hide success message after 3 seconds
        setTimeout(() => {
          this.upgradeSuccess = false
          this.closeModal()
        }, 3000)

        // Emit event to update parent components
        this.$emit('tier-updated', this.selectedTier)
        
      } catch (error) {
        this.upgradeError = error.message || 'Failed to upgrade plan'
        console.error('Upgrade failed:', error)
      } finally {
        this.isUpgrading = false
      }
    },

    closeModal() {
      this.showUpgradeModal = false
      this.selectedTier = null
      this.upgradeError = null
      this.upgradeSuccess = false
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
</style>
