<template>
  <div class="not-found-container">
    <div class="not-found-background">
      <div class="not-found-card">
        <div class="not-found-header">
          <div class="logo-section">
            <div class="logo-icon">
              <svg viewBox="0 0 24 24" width="40" height="40">
                <path fill="#00D4AA" d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
              </svg>
            </div>
            <h1>EMQX Topic Hub</h1>
          </div>
          <div class="error-code">404</div>
          <div class="error-message">
            <h2>页面未找到</h2>
            <p>抱歉，您访问的页面不存在或已被移除。</p>
          </div>
        </div>
        
        <div class="error-actions">
          <el-button type="primary" size="large" class="action-button" @click="goHome">
            <el-icon><House /></el-icon>
            返回首页
          </el-button>
          <el-button size="large" class="action-button secondary" @click="goBack">
            <el-icon><ArrowLeft /></el-icon>
            返回上页
          </el-button>
        </div>
        
        <div class="not-found-footer">
          <p>如果问题持续存在，请联系系统管理员</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { House, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.not-found-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, var(--bg-dark) 0%, var(--bg-sidebar) 50%, var(--gray-800) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.not-found-background::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: 
    radial-gradient(circle at 20% 80%, rgba(0, 212, 170, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(99, 102, 241, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(0, 212, 170, 0.08) 0%, transparent 50%);
  animation: float 8s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  33% { transform: translateY(-10px) rotate(1deg); }
  66% { transform: translateY(5px) rotate(-1deg); }
}

.not-found-card {
  width: 480px;
  padding: var(--spacing-2xl);
  background: rgba(255, 255, 255, 0.98);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-2xl);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
  text-align: center;
}

.not-found-header {
  margin-bottom: var(--spacing-xl);
}

.logo-section {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--spacing-lg);
}

.logo-icon {
  margin-right: var(--spacing-md);
}

.not-found-header h1 {
  color: var(--text-primary);
  font-size: 28px;
  font-weight: 700;
  margin: 0;
  letter-spacing: -0.5px;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.error-code {
  font-size: 96px;
  font-weight: 900;
  color: var(--text-primary);
  margin: var(--spacing-lg) 0;
  line-height: 1;
  background: var(--primary-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.8; }
}

.error-message {
  margin-bottom: var(--spacing-xl);
}

.error-message h2 {
  color: var(--text-primary);
  font-size: 24px;
  font-weight: 600;
  margin: 0 0 var(--spacing-md) 0;
}

.error-message p {
  color: var(--text-secondary);
  font-size: 15px;
  line-height: 1.6;
  margin: 0;
  font-weight: 500;
}

.error-actions {
  display: flex;
  gap: var(--spacing-md);
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: var(--spacing-lg);
}

.action-button {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: var(--radius-md);
  position: relative;
  overflow: hidden;
  min-width: 140px;
}

.action-button:not(.secondary) {
  background: var(--primary-gradient);
  border: none;
}

.action-button.secondary {
  background: var(--bg-primary);
  border: 1px solid var(--border-light);
  color: var(--text-primary);
}

.action-button.secondary:hover {
  background: var(--bg-secondary);
  border-color: var(--primary-color);
  transform: translateY(-1px);
}

.not-found-footer {
  text-align: center;
  color: var(--text-tertiary);
  font-size: 13px;
  padding: var(--spacing-md) 0;
  border-top: 1px solid var(--border-light);
  margin-top: var(--spacing-lg);
  position: relative;
}

.not-found-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 1px;
  background: var(--primary-gradient);
}

.not-found-footer p {
  margin: 0;
  font-weight: 500;
  opacity: 0.8;
  transition: opacity var(--transition-normal);
}

.not-found-footer:hover p {
  opacity: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .not-found-card {
    width: 90%;
    max-width: 420px;
    padding: var(--spacing-xl);
    margin: var(--spacing-md);
  }

  .not-found-header h1 {
    font-size: 24px;
  }

  .error-code {
    font-size: 72px;
  }

  .error-message h2 {
    font-size: 20px;
  }

  .error-message p {
    font-size: 14px;
  }

  .logo-icon {
    margin-right: var(--spacing-sm);
  }

  .action-button {
    height: 44px;
    font-size: 15px;
    min-width: 120px;
  }
}

@media (max-width: 480px) {
  .not-found-card {
    width: 95%;
    padding: var(--spacing-lg);
    margin: var(--spacing-sm);
  }

  .not-found-header {
    margin-bottom: var(--spacing-lg);
  }

  .not-found-header h1 {
    font-size: 20px;
  }

  .error-code {
    font-size: 60px;
  }

  .error-message h2 {
    font-size: 18px;
  }

  .error-message p {
    font-size: 13px;
  }

  .logo-section {
    margin-bottom: var(--spacing-md);
  }

  .logo-icon {
    margin-right: var(--spacing-xs);
  }

  .error-actions {
    flex-direction: column;
    align-items: center;
    gap: var(--spacing-sm);
  }

  .action-button {
    width: 100%;
    max-width: 200px;
    height: 42px;
    font-size: 14px;
  }

  .not-found-footer {
    font-size: 12px;
    padding: var(--spacing-sm) 0;
    margin-top: var(--spacing-md);
  }
}

@media (max-width: 360px) {
  .not-found-card {
    width: 98%;
    padding: var(--spacing-md);
  }

  .not-found-header h1 {
    font-size: 18px;
  }

  .error-code {
    font-size: 48px;
  }

  .action-button {
    height: 40px;
    font-size: 13px;
  }
}
</style>