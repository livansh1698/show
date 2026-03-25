<template>
  <div class="login-container" @mousedown="onMouseDown">
    <WindowControls class="login-window-ctrls"/>
    <!-- 左侧品牌区域 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-identity">
          <div class="brand-logo">
            <img src="@/assets/logo.png" alt="Show Logo" width="80" />
          </div>
          <h1 class="brand-name">Show</h1>
          <p class="brand-slogan">剪发更专注，管店更轻松</p>
        </div>

        <div class="brand-features">
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>留住每一位顾客</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>激活每一笔消费</span>
          </div>
          <div class="feature-item">
            <div class="feature-dot"></div>
            <span>锁客源，更锁人心</span>
          </div>
        </div>
      </div>
      <div class="brand-decoration">
        <div class="deco-circle deco-circle-1"></div>
        <div class="deco-circle deco-circle-2"></div>
        <div class="deco-circle deco-circle-3"></div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-form-area">
      <div class="login-card" :class="{ 'shake': showError }">
        <div class="card-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账户以继续</p>
        </div>

        <form @submit.prevent="handleLogin" class="login-form">
          <div class="form-group" :class="{ 'focused': usernameFocused, 'filled': username }">
            <label class="form-label">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                <circle cx="12" cy="7" r="4"/>
              </svg>
              用户名
            </label>
            <input
                type="text"
                v-model="username"
                placeholder="请输入用户名"
                @focus="usernameFocused = true"
                @blur="usernameFocused = false"
                autocomplete="off"
            />
          </div>

          <div class="form-group" :class="{ 'focused': passwordFocused, 'filled': password }">
            <label class="form-label">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
              </svg>
              密码
            </label>
            <div class="password-wrapper">
              <input
                  :type="showPassword ? 'text' : 'password'"
                  v-model="password"
                  placeholder="请输入密码"
                  @focus="passwordFocused = true"
                  @blur="passwordFocused = false"
                  autocomplete="off"
              />
              <button type="button" class="toggle-pwd" @click="showPassword = !showPassword">
                <svg v-if="!showPassword" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                  <circle cx="12" cy="12" r="3"/>
                </svg>
                <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2">
                  <path
                      d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                  <line x1="1" y1="1" x2="23" y2="23"/>
                </svg>
              </button>
            </div>
          </div>

          <div class="form-options">
            <label class="checkbox-label">
              <input type="checkbox" v-model="rememberMe"/>
              <span class="checkmark"></span>
              记住我
            </label>
          </div>

          <button type="submit" class="login-btn" :disabled="loading">
            <span v-if="!loading">登 录</span>
            <span v-else class="loading-dots">
              <i></i><i></i><i></i>
            </span>
          </button>

          <button type="button" class="register-btn" :disabled="loading" @click="openRegisterDialog">
            注 册
          </button>

          <transition name="fade">
            <div v-if="errorMsg" class="error-msg" :class="{ success: msgType === 'success' }">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <line x1="15" y1="9" x2="9" y2="15"/>
                <line x1="9" y1="9" x2="15" y2="15"/>
              </svg>
              {{ errorMsg }}
            </div>
          </transition>
        </form>
      </div>
    </div>

    <div v-if="showRegisterDialog" class="dialog-mask" @click.self="closeRegisterDialog">
      <div class="dialog-card" :class="{ shake: registerShake }">
        <h3>注册店长账号</h3>
        <p>创建后可直接使用该账号登录</p>
        <div class="dialog-form">
          <input v-model.trim="registerForm.username" type="text" placeholder="用户名" />
          <input v-model.trim="registerForm.password" type="password" placeholder="密码（至少6位）" />
          <input v-model.trim="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" />
          <input v-model.trim="registerForm.nickname" type="text" maxlength="6" placeholder="昵称（最多6个字）" />
        </div>
        <div v-if="registerMsg" class="dialog-msg" :class="{ success: registerMsgType === 'success' }">
          {{ registerMsg }}
        </div>
        <div class="dialog-actions">
          <button class="login-btn small-btn" :disabled="registering" @click="handleRegister">
            <span v-if="!registering">确 认 注 册</span>
            <span v-else class="loading-dots"><i></i><i></i><i></i></span>
          </button>
          <button class="register-btn small-btn" :disabled="registering" @click="closeRegisterDialog">取 消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import WindowControls from '@/components/WindowControls.vue'
import {useWindowDrag} from '@/composables/useWindowDrag'

const router = useRouter()
const {onMouseDown} = useWindowDrag()

const username = ref('')
const password = ref('')
const showPassword = ref(false)
const rememberMe = ref(false)
const loading = ref(false)
const errorMsg = ref('')
const msgType = ref('error')
const showError = ref(false)
const usernameFocused = ref(false)
const passwordFocused = ref(false)
const showRegisterDialog = ref(false)
const registering = ref(false)
const registerShake = ref(false)
const registerMsg = ref('')
const registerMsgType = ref('error')
const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})

async function handleLogin() {
  errorMsg.value = ''

  if (!username.value.trim()) {
    triggerError('请输入用户名')
    return
  }
  if (!password.value.trim()) {
    triggerError('请输入密码')
    return
  }

  loading.value = true

  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
        username: username.value,
        password: password.value
      })
    })

    const data = await res.json()

    if (data.success) {
      const token = data?.data?.token
      const user = data?.data?.user
      if (!token || token.split('.').length !== 3) {
        triggerError('登录返回的 token 非法')
        return
      }
      sessionStorage.setItem('token', token)
      sessionStorage.setItem('user', JSON.stringify(user || {}))
      router.push('/dashboard')
    } else {
      triggerError(data.message || '登录失败')
    }
  } catch (err) {
    triggerError('网络连接错误，请重试')
  } finally {
    loading.value = false
  }
}

function triggerError(msg) {
  msgType.value = 'error'
  errorMsg.value = msg
  showError.value = true
  setTimeout(() => {
    showError.value = false
  }, 500)
}

function openRegisterDialog() {
  showRegisterDialog.value = true
  registerMsg.value = ''
}

function closeRegisterDialog() {
  showRegisterDialog.value = false
  registerShake.value = false
  registerMsg.value = ''
  registerForm.value = { username: '', password: '', confirmPassword: '', nickname: '' }
}

function triggerRegisterMsg(msg, type = 'error') {
  registerMsgType.value = type
  registerMsg.value = msg
  if (type === 'error') {
    registerShake.value = true
    setTimeout(() => {
      registerShake.value = false
    }, 420)
  }
}

async function handleRegister() {
  if (!registerForm.value.username || !registerForm.value.password || !registerForm.value.confirmPassword || !registerForm.value.nickname) {
    triggerRegisterMsg('请完整填写注册信息')
    return
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    triggerRegisterMsg('两次密码不一致')
    return
  }
  if (registerForm.value.password.length < 6) {
    triggerRegisterMsg('密码长度不能少于6位')
    return
  }
  if (registerForm.value.nickname.length > 6) {
    triggerRegisterMsg('昵称最多6个字')
    return
  }
  registering.value = true
  try {
    const res = await fetch('/api/auth/register', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(registerForm.value)
    })
    const data = await res.json()
    if (data.success) {
      const loginRes = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
          username: registerForm.value.username,
          password: registerForm.value.password
        })
      })
      const loginData = await loginRes.json()
      if (loginData.success) {
        const token = loginData?.data?.token
        const user = loginData?.data?.user
        if (!token || token.split('.').length !== 3) {
          triggerRegisterMsg('自动登录失败：token 非法')
          return
        }
        sessionStorage.setItem('token', token)
        sessionStorage.setItem('user', JSON.stringify(user || {}))
        closeRegisterDialog()
        router.push('/dashboard')
      } else {
        triggerRegisterMsg(loginData.message || '自动登录失败，请手动登录')
      }
    } else {
      triggerRegisterMsg(data.message || '注册失败')
    }
  } catch (e) {
    triggerRegisterMsg('网络连接错误，请重试')
  } finally {
    registering.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100%;
  display: flex;
  position: relative;
}

.login-window-ctrls {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 1000;
  color: var(--text-secondary);
}

/* ===== 左侧品牌区 ===== */
.login-brand {
  width: 420px;
  min-width: 420px;
  height: 100%;
  background: linear-gradient(145deg, #4f46e5, #6366f1, #818cf8);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  padding: 40px;
  animation: slideUp 0.6s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 40px;
}

.brand-identity {
  text-align: center;
}

.brand-logo {
  margin-bottom: 20px;
  filter: drop-shadow(0 10px 15px rgba(0, 0, 0, 0.2));
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.brand-name {
  font-size: 42px;
  font-weight: 800;
  color: white;
  letter-spacing: 4px;
  margin-bottom: 8px;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}

.brand-slogan {
  font-size: var(--font-size-md);
  color: rgba(255, 255, 255, 0.85);
  letter-spacing: 2px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;
  padding: 24px 32px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: var(--radius-lg);
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.9);
  font-size: var(--font-size-sm);
}

.feature-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.7);
}

/* 装饰圆圈 */
.brand-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  border-radius: 50%;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.deco-circle-1 {
  width: 300px;
  height: 300px;
  top: -80px;
  right: -100px;
  background: rgba(255, 255, 255, 0.04);
}

.deco-circle-2 {
  width: 200px;
  height: 200px;
  bottom: -60px;
  left: -40px;
  background: rgba(255, 255, 255, 0.03);
}

.deco-circle-3 {
  width: 120px;
  height: 120px;
  bottom: 100px;
  right: 40px;
  background: rgba(255, 255, 255, 0.05);
}

/* ===== 右侧表单区 ===== */
.login-form-area {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-primary);
  padding: 40px;
}

.login-card {
  width: 100%;
  max-width: 380px;
  animation: slideUp 0.5s ease;
}

.login-card.shake {
  animation: shake 0.4s ease;
}

@keyframes shake {
  0%, 100% {
    transform: translateX(0);
  }
  25% {
    transform: translateX(-8px);
  }
  50% {
    transform: translateX(8px);
  }
  75% {
    transform: translateX(-4px);
  }
}

.card-header {
  margin-bottom: 36px;
}

.card-header h2 {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 8px;
}

.card-header p {
  font-size: var(--font-size-md);
  color: var(--text-secondary);
}

/* Form */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.form-group {
  position: relative;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--text-secondary);
  margin-bottom: 8px;
  transition: color var(--transition-fast);
}

.form-group.focused .form-label {
  color: var(--accent);
}

.form-group input[type="text"],
.form-group input[type="password"] {
  width: 100%;
  height: 46px;
  padding: 0 16px;
  font-size: var(--font-size-md);
  color: var(--text-primary);
  background: var(--bg-secondary);
  border: 1.5px solid var(--border);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  outline: none;
}

.form-group input::placeholder {
  color: var(--text-muted);
}

.form-group.focused input {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-light);
}

.password-wrapper {
  position: relative;
}

.password-wrapper input {
  padding-right: 44px;
}

.toggle-pwd {
  position: absolute;
  right: 2px;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
  transition: color var(--transition-fast);
  border-radius: var(--radius-sm);
}

.toggle-pwd:hover {
  color: var(--text-primary);
}

/* Options */
.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  cursor: pointer;
  user-select: none;
}

.checkbox-label input[type="checkbox"] {
  display: none;
}

.checkmark {
  width: 16px;
  height: 16px;
  border: 1.5px solid var(--border);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all var(--transition-fast);
  position: relative;
}

.checkbox-label input:checked + .checkmark {
  background: var(--accent);
  border-color: var(--accent);
}

.checkbox-label input:checked + .checkmark::after {
  content: '';
  width: 4px;
  height: 8px;
  border: solid white;
  border-width: 0 2px 2px 0;
  transform: rotate(45deg);
  position: absolute;
  top: 1px;
}

/* Login button */
.login-btn {
  width: 100%;
  height: 48px;
  font-size: var(--font-size-lg);
  font-weight: 600;
  letter-spacing: 4px;
  color: white;
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  border-radius: var(--radius-md);
  transition: all var(--transition-normal);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.35);
  margin-top: 4px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(99, 102, 241, 0.45);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.3);
}

.login-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.register-btn {
  width: 100%;
  height: 44px;
  font-size: var(--font-size-md);
  font-weight: 600;
  letter-spacing: 2px;
  color: #4f46e5;
  background: #f5f6ff;
  border: 1px solid #d6d9ff;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.register-btn:hover:not(:disabled) {
  background: #eceeff;
  border-color: #bfc5ff;
}

.register-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* Loading dots */
.loading-dots {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.loading-dots i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: white;
  animation: pulse 1s ease-in-out infinite;
}

.loading-dots i:nth-child(2) {
  animation-delay: 0.15s;
}

.loading-dots i:nth-child(3) {
  animation-delay: 0.3s;
}

/* Error */
.error-msg {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: var(--font-size-sm);
  color: var(--danger);
  padding: 10px 14px;
  background: rgba(239, 68, 68, 0.08);
  border-radius: var(--radius-sm);
  border: 1px solid rgba(239, 68, 68, 0.15);
}

.error-msg.success {
  color: #2563eb;
  background: rgba(79, 70, 229, 0.08);
  border-color: rgba(79, 70, 229, 0.18);
}

.dialog-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
}

.dialog-card {
  width: 380px;
  max-width: calc(100vw - 40px);
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  padding: 22px;
  box-shadow: var(--shadow-lg);
}

.dialog-card.shake {
  animation: shake 0.38s ease;
}

.dialog-card h3 {
  font-size: var(--font-size-lg);
  color: var(--text-primary);
}

.dialog-card p {
  margin-top: 6px;
  margin-bottom: 14px;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.dialog-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dialog-form input {
  width: 100%;
  height: 42px;
  padding: 0 14px;
  font-size: var(--font-size-sm);
  border: 1.5px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--bg-secondary);
}

.dialog-form input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-light);
}

.dialog-actions {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.dialog-msg {
  margin-top: 10px;
  padding: 9px 12px;
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  color: var(--danger);
  background: rgba(239, 68, 68, 0.08);
  border: 1px solid rgba(239, 68, 68, 0.16);
}

.dialog-msg.success {
  color: #4f46e5;
  background: rgba(79, 70, 229, 0.08);
  border-color: rgba(79, 70, 229, 0.18);
}

.small-btn {
  height: 42px;
}
</style>
