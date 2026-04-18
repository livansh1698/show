<template>
  <div class="dashboard" @mousedown="onMouseDown">
    <WindowControls class="dashboard-window-ctrls" />

    <aside class="sidebar">
      <div class="sidebar-profile">
        <div class="avatar">
          <img src="@/assets/avatar.png" alt="店长头像" />
        </div>
        <div class="profile-info">
          <div class="profile-name">店长</div>
          <div class="profile-role">理发店会员系统</div>
        </div>
      </div>

      <nav class="sidebar-nav">
        <div v-for="item in navItems" :key="item.key" class="nav-item" :class="{ active: activeNav === item.key }" @click="activeNav = item.key">
          <span>{{ item.label }}</span>
        </div>
      </nav>

      <div class="sidebar-bottom">
        <div class="nav-item logout-item" @click="handleLogout">退出登录</div>
      </div>
    </aside>

    <main class="main-content">
      <header class="content-header">
        <div>
          <h1 class="page-title">{{ currentTitle }}</h1>
          <p class="page-desc">{{ currentDate }}</p>
        </div>
        <div class="header-actions">
          <input v-model.trim="keyword" class="search-input" :placeholder="searchPlaceholder" />
          <button class="secondary-btn" @click="openSystemDialog">系统管理</button>
          <button class="secondary-btn" @click="refreshAll">刷新</button>
        </div>
      </header>

      <section v-if="activeNav === 'dashboard' || activeNav === 'reports'" class="stats-section">
        <div class="stat-card">
          <div class="stat-value">{{ summary.activeCustomers || 0 }}</div>
          <div class="stat-label">活跃会员</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ money(summary.totalBalance) }}</div>
          <div class="stat-label">账户总余额</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ money(summary.todayRecharge) }}</div>
          <div class="stat-label">今日充值</div>
        </div>
        <div class="stat-card">
          <div class="stat-value">{{ money(summary.todayConsume) }}</div>
          <div class="stat-label">今日消费</div>
        </div>
      </section>

      <section v-if="activeNav === 'dashboard'" class="card dashboard-card">
        <h3 class="card-title">经营状态</h3>
        <div class="overview-grid">
          <div class="overview-panel quote-panel">
            <div class="overview-kicker">今日语录</div>
            <p class="quote-text">{{ currentQuote }}</p>
          </div>
          <div class="overview-panel">
            <div class="overview-kicker">今日目标</div>
            <div class="target-header">
              <span>{{ money(todayFlow) }} / {{ money(dailyTarget) }}</span>
              <span>{{ targetProgress }}%</span>
            </div>
            <div class="target-track">
              <div class="target-bar" :style="{ width: `${targetProgress}%` }"></div>
            </div>
            <p class="muted">{{ targetGap <= 0 ? '今日目标已达成，继续冲刺！' : `还差 ${money(targetGap)} 达成今日目标` }}</p>
            <div class="target-actions">
              <button class="secondary-btn" @click="dailyTarget = Math.max(500, dailyTarget - 500)">目标 -500</button>
              <button class="secondary-btn" @click="dailyTarget += 500">目标 +500</button>
            </div>
          </div>
          <div class="overview-panel lucky-panel" :class="{ animating: luckyAnimating }">
            <div class="overview-kicker">发财签</div>
            <p class="lucky-text">{{ luckyMessage }}</p>
            <div class="lucky-sparkles" aria-hidden="true">
              <span class="spark spark-1"></span>
              <span class="spark spark-2"></span>
              <span class="spark spark-3"></span>
            </div>
            <button class="primary-btn lucky-draw-btn" :disabled="luckyAnimating" @click="drawLucky">
              {{ luckyAnimating ? '开运中...' : '抽一签' }}
            </button>
          </div>
          <div class="overview-panel tips-panel">
            <div class="overview-kicker">今日建议</div>
            <p v-for="tip in operationTips" :key="tip" class="tips-item">{{ tip }}</p>
          </div>
        </div>
      </section>

      <section v-if="activeNav === 'customers'" class="content-grid list-section customers-section">
        <div class="card">
          <h3 class="card-title">{{ customerForm.id ? '编辑会员' : '新增会员' }}</h3>
          <div class="form-grid">
            <input v-model.trim="customerForm.name" placeholder="会员姓名" />
            <input v-model.trim="customerForm.phone" placeholder="手机号" @input="syncVerifyCodeByPhone" />
            <input v-model.trim="customerForm.verifyCode" maxlength="4" placeholder="校验码(4位，默认手机号后4位)" />
            <input v-model.number="customerForm.initialRechargeAmount" type="number" min="0" step="0.01" :disabled="!!customerForm.id" placeholder="初次充值金额(仅新增)" />
            <textarea v-model.trim="customerForm.remark" placeholder="备注"></textarea>
          </div>
          <div class="btn-row">
            <button class="primary-btn" @click="saveCustomer">{{ customerForm.id ? '保存' : '新增' }}</button>
            <button class="secondary-btn" @click="resetCustomerForm">清空</button>
            <button class="secondary-btn" @click="exportCustomers">导出会员 CSV</button>
          </div>
        </div>
        <div class="card list-card">
          <h3 class="card-title">会员列表</h3>
          <div class="list-filter-row">
            <input v-model.trim="customerListKeyword" placeholder="按会员姓名/手机号搜索" />
          </div>
          <div class="table-wrap">
            <table class="customer-table">
              <colgroup>
                <col style="width: 16%" />
                <col style="width: 20%" />
                <col style="width: 12%" />
                <col style="width: 12%" />
                <col style="width: 16%" />
                <col style="width: 24%" />
              </colgroup>
              <thead>
                <tr>
                  <th>姓名</th>
                  <th>手机号</th>
                  <th>校验码</th>
                  <th>状态</th>
                  <th>余额</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="c in customers" :key="c.id">
                  <td>{{ c.name }}</td>
                  <td>{{ c.phone }}</td>
                  <td>
                    <button class="text-btn" @click="toggleVerifyCode(c.id)">
                      {{ customerVerifyVisible[c.id] ? c.verifyCode : '****' }}
                    </button>
                  </td>
                  <td>{{ c.status === 'active' ? '正常' : '停用' }}</td>
                  <td>{{ money(balanceMap[c.id]) }}</td>
                  <td>
                    <button class="text-btn" @click="editCustomer(c)">编辑</button>
                    <button class="text-btn danger" @click="toggleCustomer(c.id)">停用/恢复</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pager">
            <button class="secondary-btn" :disabled="customerPager.page <= 1" @click="changePage('customers', -1)">上一页</button>
            <span>第 {{ customerPager.page }} / {{ customerPager.totalPages }} 页（共 {{ customerPager.total }} 条）</span>
            <button class="secondary-btn" :disabled="customerPager.page >= customerPager.totalPages" @click="changePage('customers', 1)">下一页</button>
          </div>
        </div>
      </section>

      <section v-if="activeNav === 'employees'" class="content-grid list-section">
        <div class="card">
          <h3 class="card-title">{{ employeeForm.id ? '编辑员工' : '新增员工' }}</h3>
          <div class="form-grid">
            <input v-model.trim="employeeForm.name" placeholder="员工姓名" />
          </div>
          <div class="btn-row">
            <button class="primary-btn" @click="saveEmployee">{{ employeeForm.id ? '保存' : '新增' }}</button>
            <button class="secondary-btn" @click="resetEmployeeForm">清空</button>
          </div>
        </div>
        <div class="card list-card">
          <h3 class="card-title">员工列表</h3>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>姓名</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="e in employees" :key="e.id">
                  <td>{{ e.name }}</td>
                  <td>{{ e.status === 'active' ? '在岗' : '停用' }}</td>
                  <td>
                    <button class="text-btn" @click="editEmployee(e)">编辑</button>
                    <button class="text-btn danger" @click="toggleEmployee(e.id)">停用/恢复</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pager">
            <button class="secondary-btn" :disabled="employeePager.page <= 1" @click="changePage('employees', -1)">上一页</button>
            <span>第 {{ employeePager.page }} / {{ employeePager.totalPages }} 页（共 {{ employeePager.total }} 条）</span>
            <button class="secondary-btn" :disabled="employeePager.page >= employeePager.totalPages" @click="changePage('employees', 1)">下一页</button>
          </div>
        </div>
      </section>

      <section v-if="activeNav === 'transactions'" class="content-grid list-section">
        <div class="card">
          <h3 class="card-title">会员充值</h3>
          <div class="form-grid">
            <div class="combo">
              <input
                v-model.trim="rechargeMemberKeyword"
                placeholder="选择会员(可输入姓名/手机号搜索)"
                @focus="rechargeDropdownOpen = true"
                @blur="closeRechargeDropdown"
                @input="onRechargeMemberInput"
              />
              <div v-if="rechargeDropdownOpen" class="combo-menu">
                <div v-for="c in rechargeCandidateCustomers" :key="c.id" class="combo-item" @mousedown.prevent="selectRechargeCustomer(c)">
                  {{ c.name }} / {{ c.phone }}
                </div>
                <div v-if="!rechargeCandidateCustomers.length" class="combo-empty">无匹配会员</div>
              </div>
            </div>
            <input v-model.number="rechargeForm.amount" type="number" min="0" step="0.01" placeholder="充值金额" />
            <input v-model.trim="rechargeForm.remark" placeholder="备注" />
          </div>
          <div class="btn-row">
            <button class="primary-btn" @click="createRecharge">确认充值</button>
          </div>
        </div>

        <div class="card">
          <h3 class="card-title">会员消费</h3>
          <div class="form-grid">
            <div class="combo">
              <input
                v-model.trim="consumeMemberKeyword"
                placeholder="选择会员(可输入姓名/手机号搜索)"
                @focus="consumeDropdownOpen = true"
                @blur="closeConsumeDropdown"
                @input="onConsumeMemberInput"
              />
              <div v-if="consumeDropdownOpen" class="combo-menu">
                <div v-for="c in consumeCandidateCustomers" :key="c.id" class="combo-item" @mousedown.prevent="selectConsumeCustomer(c)">
                  {{ c.name }} / {{ c.phone }} / 余额 {{ balanceMap[c.id] == null ? '--' : money(balanceMap[c.id]) }}
                </div>
                <div v-if="!consumeCandidateCustomers.length" class="combo-empty">无匹配会员</div>
              </div>
            </div>
            <select v-model="consumeForm.employeeId">
              <option value="">选择员工</option>
              <option v-for="e in activeEmployees" :key="e.id" :value="e.id">{{ e.name }}</option>
            </select>
            <select v-model="consumeForm.serviceTypeId" @change="fillServicePrice">
              <option value="">选择服务</option>
              <option v-for="s in activeServices" :key="s.id" :value="s.id">{{ s.name }} / 默认 {{ money(s.price) }}</option>
            </select>
            <input v-model.number="consumeForm.amount" type="number" min="0" step="0.01" placeholder="消费金额" />
            <input v-model.trim="consumeForm.verifyCode" maxlength="4" placeholder="校验码(默认手机号后4位)" />
            <input v-model.trim="consumeForm.remark" placeholder="备注" />
          </div>
          <div class="btn-row">
            <button class="primary-btn" @click="createConsume">确认扣款</button>
            <button class="secondary-btn" @click="exportTransactions">导出交易 CSV</button>
          </div>
        </div>

        <div class="card full list-card">
          <h3 class="card-title">交易流水</h3>
          <div class="table-wrap">
            <table class="transaction-table">
              <colgroup>
                <col style="width: 22%" />
                <col style="width: 12%" />
                <col style="width: 16%" />
                <col style="width: 14%" />
                <col style="width: 36%" />
              </colgroup>
              <thead>
                <tr>
                  <th>时间</th>
                  <th>类型</th>
                  <th>会员</th>
                  <th>金额</th>
                  <th>详情</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="t in transactions" :key="t.id">
                  <td>{{ dateTime(t.createdAt) }}</td>
                  <td>{{ t.type === 'recharge' ? '充值' : '消费' }}</td>
                  <td>{{ t.customerName }}</td>
                  <td>{{ money(t.amount) }}</td>
                  <td>{{ t.detail }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pager">
            <button class="secondary-btn" :disabled="transactionPager.page <= 1" @click="changePage('transactions', -1)">上一页</button>
            <span>第 {{ transactionPager.page }} / {{ transactionPager.totalPages }} 页（共 {{ transactionPager.total }} 条）</span>
            <button class="secondary-btn" :disabled="transactionPager.page >= transactionPager.totalPages" @click="changePage('transactions', 1)">下一页</button>
          </div>
        </div>
      </section>

      <section v-if="activeNav === 'reports'" class="content-grid list-section">
        <div class="card full">
          <h3 class="card-title">报表筛选</h3>
          <div class="filter-row">
            <input v-model="report.startDate" type="date" />
            <input v-model="report.endDate" type="date" />
            <button class="secondary-btn" @click="loadReports">查询</button>
            <button class="secondary-btn" @click="exportPerformance">导出员工业绩 CSV</button>
          </div>
        </div>
        <div class="card report-summary-card">
          <h3 class="card-title">汇总</h3>
          <div class="summary-metrics">
            <div class="summary-item">
              <div class="summary-label">充值总额</div>
              <div class="summary-value">{{ money(reportSummary.total_recharge) }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">消费总额</div>
              <div class="summary-value">{{ money(reportSummary.total_consume) }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">净收入</div>
              <div class="summary-value">{{ money(Number(reportSummary.total_recharge || 0) - Number(reportSummary.total_consume || 0)) }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">总会员</div>
              <div class="summary-value">{{ reportSummary.total_customers || 0 }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">新增会员</div>
              <div class="summary-value">{{ reportSummary.new_customers || 0 }}</div>
            </div>
            <div class="summary-item">
              <div class="summary-label">活跃会员</div>
              <div class="summary-value">{{ reportSummary.active_customers || 0 }}</div>
            </div>
          </div>
        </div>
        <div class="card report-service-card">
          <h3 class="card-title">服务分布</h3>
          <div v-if="serviceChartData.length" class="service-chart">
            <div v-for="row in serviceChartData" :key="row.serviceTypeId" class="service-chart-row">
              <div class="service-chart-meta">
                <div class="service-chart-name">{{ row.serviceName }}</div>
                <div class="service-chart-sub">{{ row.total_count }} 单</div>
              </div>
              <div class="service-chart-track">
                <div class="service-chart-bar" :style="{ width: `${row.barWidth}%` }"></div>
              </div>
              <div class="service-chart-value">{{ money(row.total_amount) }}</div>
            </div>
          </div>
          <p v-else class="muted">暂无服务消费数据</p>
        </div>
        <div class="card full list-card">
          <h3 class="card-title">员工业绩</h3>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>员工</th>
                  <th>订单数</th>
                  <th>总金额</th>
                  <th>客单价</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in pagedPerformance" :key="row.employeeId">
                  <td>{{ row.employeeName }}</td>
                  <td>{{ row.total_count }}</td>
                  <td>{{ money(row.total_amount) }}</td>
                  <td>{{ money(row.avg_amount) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pager">
            <button class="secondary-btn" :disabled="performancePager.page <= 1" @click="changePage('performance', -1)">上一页</button>
            <span>第 {{ performancePager.page }} / {{ performanceTotalPages }} 页（共 {{ performance.length }} 条）</span>
            <button class="secondary-btn" :disabled="performancePager.page >= performanceTotalPages" @click="changePage('performance', 1)">下一页</button>
          </div>
        </div>
      </section>

      <section v-if="activeNav === 'audit'" class="card list-card audit-section">
        <h3 class="card-title">审计日志</h3>
        <div class="table-wrap">
          <table class="audit-table">
            <colgroup>
              <col style="width: 22%" />
              <col style="width: 16%" />
              <col style="width: 16%" />
              <col style="width: 46%" />
            </colgroup>
            <thead>
              <tr>
                <th>时间</th>
                <th>动作</th>
                <th>实体</th>
                <th>详情</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in pagedAuditLogs" :key="log.id">
                <td>{{ dateTime(log.createdAt) }}</td>
                <td>{{ log.action }}</td>
                <td>{{ log.entityType }}</td>
                <td>{{ log.detail }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="pager">
          <button class="secondary-btn" :disabled="auditPager.page <= 1" @click="changePage('audit', -1)">上一页</button>
          <span>第 {{ auditPager.page }} / {{ auditTotalPages }} 页（共 {{ auditLogs.length }} 条）</span>
          <button class="secondary-btn" :disabled="auditPager.page >= auditTotalPages" @click="changePage('audit', 1)">下一页</button>
        </div>
      </section>

      <section v-if="activeNav === 'settings'" class="content-grid list-section">
        <div class="card">
          <h3 class="card-title">{{ serviceForm.id ? '编辑服务' : '新增服务' }}</h3>
          <div class="form-grid">
            <input v-model.trim="serviceForm.name" placeholder="服务名称" />
            <input v-model.number="serviceForm.price" type="number" min="0" step="0.01" placeholder="默认价格" />
          </div>
          <div class="btn-row">
            <button class="primary-btn" @click="saveService">{{ serviceForm.id ? '保存' : '新增' }}</button>
            <button class="secondary-btn" @click="resetServiceForm">清空</button>
          </div>
        </div>
        <div class="card list-card">
          <h3 class="card-title">服务类型</h3>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>名称</th>
                  <th>价格</th>
                  <th>状态</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="s in pagedServices" :key="s.id">
                  <td>{{ s.name }}</td>
                  <td>{{ money(s.price) }}</td>
                  <td>{{ s.status === 'active' ? '启用' : '停用' }}</td>
                  <td>
                    <button class="text-btn" @click="editService(s)">编辑</button>
                    <button class="text-btn danger" @click="toggleService(s.id)">停用/恢复</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pager">
            <button class="secondary-btn" :disabled="servicePager.page <= 1" @click="changePage('services', -1)">上一页</button>
            <span>第 {{ servicePager.page }} / {{ serviceTotalPages }} 页（共 {{ services.length }} 条）</span>
            <button class="secondary-btn" :disabled="servicePager.page >= serviceTotalPages" @click="changePage('services', 1)">下一页</button>
          </div>
        </div>
      </section>

      <section v-if="activeNav === 'help'" class="card help-card">
        <h3 class="card-title">使用帮助</h3>
        <div v-if="accessInfo.url" class="help-access-tip">
          <div class="help-access-title">友情提示</div>
          <div class="help-access-desc">
            当前服务地址：<strong>{{ accessInfo.ip }}:{{ accessInfo.port }}</strong>，可在浏览器打开
            <code>{{ accessInfo.url }}</code> 访问 Web 页面。
          </div>
        </div>
        <div class="help-wrap">
          <div class="help-item">
            <h4>1. 添加客户（会员）</h4>
            <p>进入“会员管理”，填写姓名、手机号后点击“新增”。校验码可手动填写；如不填写，系统默认使用手机号后四位。</p>
          </div>
          <div class="help-item">
            <h4>2. 添加员工</h4>
            <p>进入“员工管理”，输入员工姓名并保存。停用员工不会删除历史数据，恢复后可继续使用。</p>
          </div>
          <div class="help-item">
            <h4>3. 会员充值</h4>
            <p>进入“充值消费”中的“会员充值”，在下拉框输入姓名或手机号选择会员，填写金额后确认充值。</p>
          </div>
          <div class="help-item">
            <h4>4. 会员扣费（消费）</h4>
            <p>在“会员消费”中选择会员、员工和服务，输入消费金额与校验码后确认。校验码需与会员档案一致才可扣费。</p>
          </div>
          <div class="help-item">
            <h4>5. 修改密码</h4>
            <p>点击右上角“系统管理”，在弹窗中输入旧密码和新密码完成修改。修改成功后需重新登录。</p>
          </div>
          <div class="help-item">
            <h4>6. 导出数据</h4>
            <p>在会员、交易、报表页可导出 CSV。桌面版会弹出保存窗口，网页版会直接下载文件。</p>
          </div>
          <div class="help-item help-alert">
            <h4>❗ 重要提示</h4>
            <p>客户数据存储在 <code>C:\Users\{用户}\.show</code> 目录，请勿删除该目录及其中数据库文件。</p>
          </div>
        </div>
      </section>

      <transition name="fade">
        <div v-if="toast" class="toast">{{ toast }}</div>
      </transition>

      <div v-if="systemDialogVisible" class="modal-mask" @click.self="closeSystemDialog">
        <div class="modal-card">
          <h3 class="card-title">修改密码</h3>
          <div class="form-grid single-col">
            <input v-model="pwdForm.oldPassword" type="password" placeholder="请输入旧密码" />
            <input v-model="pwdForm.newPassword" type="password" placeholder="请输入新密码（至少6位）" />
            <input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" />
          </div>
          <div class="btn-row">
            <button class="primary-btn" @click="submitChangePassword">保存</button>
            <button class="secondary-btn" @click="closeSystemDialog">取消</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import WindowControls from '@/components/WindowControls.vue'
import { useWindowDrag } from '@/composables/useWindowDrag'

const router = useRouter()
const { onMouseDown } = useWindowDrag()

const navItems = [
  { key: 'dashboard', label: '经营总览' },
  { key: 'customers', label: '会员管理' },
  { key: 'transactions', label: '充值消费' },
  { key: 'employees', label: '员工管理' },
  { key: 'reports', label: '报表分析' },
  { key: 'audit', label: '审计日志' },
  { key: 'settings', label: '系统配置' },
  { key: 'help', label: '使用帮助' }
]

const activeNav = ref('dashboard')
const keyword = ref('')
const toast = ref('')
const dailyTarget = ref(3000)
const currentQuote = ref('把每一次服务做到位，财富会按时来敲门。')
const luckyMessage = ref('今日宜稳扎稳打，客单自然上涨。')
const luckyAnimating = ref(false)
const autoVerifyCode = ref('')
const customerListKeyword = ref('')
const rechargeMemberKeyword = ref('')
const consumeMemberKeyword = ref('')
const rechargeDropdownOpen = ref(false)
const consumeDropdownOpen = ref(false)

const summary = reactive({})
const customers = ref([])
const allCustomers = ref([])
const employees = ref([])
const services = ref([])
const transactions = ref([])
const auditLogs = ref([])
const performance = ref([])
const serviceBreakdown = ref([])
const balanceMap = reactive({})
const customerVerifyVisible = reactive({})
const accessInfo = reactive({ ip: '', port: '', url: '' })
const customerPager = reactive({ page: 1, size: 8, total: 0, totalPages: 1 })
const employeePager = reactive({ page: 1, size: 8, total: 0, totalPages: 1 })
const transactionPager = reactive({ page: 1, size: 8, total: 0, totalPages: 1 })
const auditPager = reactive({ page: 1, size: 8 })
const servicePager = reactive({ page: 1, size: 8 })
const performancePager = reactive({ page: 1, size: 8 })
const systemDialogVisible = ref(false)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const report = reactive({
  startDate: dateOffset(30),
  endDate: dateOffset(0)
})
const reportSummary = reactive({})

const customerForm = reactive({ id: '', name: '', phone: '', verifyCode: '', initialRechargeAmount: '', remark: '' })
const employeeForm = reactive({ id: '', name: '' })
const rechargeForm = reactive({ customerId: '', amount: '', remark: '' })
const consumeForm = reactive({ customerId: '', employeeId: '', serviceTypeId: '', amount: '', verifyCode: '', remark: '' })
const serviceForm = reactive({ id: '', name: '', price: '' })

const activeCustomers = computed(() => allCustomers.value.filter((c) => c.status === 'active'))
const activeEmployees = computed(() => employees.value.filter((e) => e.status === 'active'))
const activeServices = computed(() => services.value.filter((s) => s.status === 'active'))
const rechargeCandidateCustomers = computed(() => {
  const kw = rechargeMemberKeyword.value.toLowerCase().trim()
  return activeCustomers.value.filter((c) => !kw || `${c.name} ${c.phone}`.toLowerCase().includes(kw))
})
const consumeCandidateCustomers = computed(() => {
  const kw = consumeMemberKeyword.value.toLowerCase().trim()
  return activeCustomers.value.filter((c) => !kw || `${c.name} ${c.phone}`.toLowerCase().includes(kw))
})
const auditTotalPages = computed(() => Math.max(1, Math.ceil(auditLogs.value.length / auditPager.size)))
const serviceTotalPages = computed(() => Math.max(1, Math.ceil(services.value.length / servicePager.size)))
const performanceTotalPages = computed(() => Math.max(1, Math.ceil(performance.value.length / performancePager.size)))
const pagedAuditLogs = computed(() => slicePage(auditLogs.value, auditPager.page, auditPager.size))
const pagedServices = computed(() => slicePage(services.value, servicePager.page, servicePager.size))
const pagedPerformance = computed(() => slicePage(performance.value, performancePager.page, performancePager.size))
const serviceChartMax = computed(() => {
  if (!serviceBreakdown.value.length) return 0
  return Math.max(...serviceBreakdown.value.map((s) => Number(s.total_amount || 0)))
})
const serviceChartData = computed(() => {
  const max = serviceChartMax.value
  return serviceBreakdown.value.map((s) => {
    const amount = Number(s.total_amount || 0)
    const baseWidth = max > 0 ? Math.round((amount / max) * 100) : 0
    const barWidth = amount > 0 ? Math.max(baseWidth, 8) : 0
    return {
      ...s,
      barWidth
    }
  })
})
const todayFlow = computed(() => Number(summary.todayRecharge || 0) + Number(summary.todayConsume || 0))
const targetProgress = computed(() => {
  const target = Number(dailyTarget.value || 0)
  if (target <= 0) return 0
  return Math.min(100, Math.round((todayFlow.value / target) * 100))
})
const targetGap = computed(() => Math.max(0, Number(dailyTarget.value || 0) - todayFlow.value))
const operationTips = computed(() => {
  const activeCustomers = Number(summary.activeCustomers || 0)
  const todayRecharge = Number(summary.todayRecharge || 0)
  const todayConsume = Number(summary.todayConsume || 0)
  const balance = Number(summary.totalBalance || 0)
  return [
    `活跃会员 ${activeCustomers} 位，建议优先回访高频到店客户。`,
    `今日充值 ${money(todayRecharge)}，可主推次卡提升复购。`,
    `今日消费 ${money(todayConsume)}，收银后可顺带做会员转化。`,
    `当前总余额 ${money(balance)}，关注沉睡会员唤醒机会。`
  ]
})

const currentTitle = computed(() => navItems.find((n) => n.key === activeNav.value)?.label || '经营总览')
const searchPlaceholder = computed(() => {
  if (activeNav.value === 'customers') return '搜索会员姓名/手机号'
  if (activeNav.value === 'employees') return '搜索员工姓名'
  if (activeNav.value === 'transactions') return '搜索流水'
  if (activeNav.value === 'audit') return '搜索审计日志'
  return '搜索当前页'
})

const currentDate = computed(() => {
  const d = new Date()
  const week = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${week[d.getDay()]}`
})

function dateOffset(days) {
  const d = new Date()
  d.setDate(d.getDate() - days)
  return d.toISOString().slice(0, 10)
}

function slicePage(list, page, size) {
  const p = Math.max(1, page)
  const s = Math.max(1, size)
  const from = (p - 1) * s
  return list.slice(from, from + s)
}

function last4(phone) {
  const p = String(phone || '').trim()
  return p.length >= 4 ? p.slice(-4) : ''
}

function money(value) {
  return new Intl.NumberFormat('zh-CN', { style: 'currency', currency: 'CNY' }).format(Number(value || 0))
}

function dateTime(v) {
  if (!v) return ''
  const d = new Date(v)
  // 1. 获取年、月、日、时、分、秒
  const year = d.getFullYear()
  const month = (d.getMonth() + 1).toString().padStart(2, '0') // 月份从0开始，需+1；补0
  const day = d.getDate().toString().padStart(2, '0')         // 补0
  const hours = d.getHours().toString().padStart(2, '0')
  const minutes = d.getMinutes().toString().padStart(2, '0')
  const seconds = d.getSeconds().toString().padStart(2, '0')

  // 2. 拼接字符串，中间加上空格
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

function notify(msg) {
  toast.value = msg
  setTimeout(() => {
    toast.value = ''
  }, 1800)
}

const luckyNotes = [
  '上上签：今日适合主推高复购服务，财气在线。',
  '中上签：老客回流运不错，主动问候会有惊喜。',
  '平稳签：节奏稳住，细节做好，现金流会更健康。',
  '进取签：适合做会员转化，今天开卡成功率偏高。',
  '丰收签：团队配合顺，连单概率提升。'
]

function drawLucky() {
  if (luckyAnimating.value) return
  luckyAnimating.value = true
  setTimeout(() => {
    luckyMessage.value = luckyNotes[Math.floor(Math.random() * luckyNotes.length)]
  }, 220)
  setTimeout(() => {
    luckyAnimating.value = false
  }, 980)
}

function openSystemDialog() {
  systemDialogVisible.value = true
}

function closeSystemDialog() {
  systemDialogVisible.value = false
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
}

async function api(path, options = {}) {
  const token = sessionStorage.getItem('token')
  const res = await fetch(path, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    ...options
  })
  const data = await res.json()
  if (!res.ok || data.success === false) {
    if (res.status === 401) {
      sessionStorage.removeItem('token')
      sessionStorage.removeItem('user')
      router.push('/login')
    }
    throw new Error(data.message || '请求失败')
  }
  return data.data
}

async function submitChangePassword() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    notify('请完整填写密码信息')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    notify('两次输入的新密码不一致')
    return
  }
  try {
    await api('/api/auth/change-password', {
      method: 'POST',
      body: JSON.stringify({
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
    })
    notify('密码修改成功，请重新登录')
    closeSystemDialog()
    handleLogout()
  } catch (e) {
    notify(e.message)
  }
}

async function loadSummary() {
  const data = await api('/api/reports/dashboard')
  Object.assign(summary, data)
}

async function loadCustomers() {
  const kw = (customerListKeyword.value || (activeNav.value === 'customers' ? keyword.value : '')).trim()
  const data = await api(
    `/api/customers?keyword=${encodeURIComponent(kw)}&page=${customerPager.page}&size=${customerPager.size}`
  )
  customers.value = data.items || []
  customerPager.page = data.page || 1
  customerPager.size = data.size || customerPager.size
  customerPager.total = data.total || 0
  customerPager.totalPages = data.totalPages || 1
  for (const c of customers.value) {
    if (customerVerifyVisible[c.id] == null) customerVerifyVisible[c.id] = false
  }
  for (const c of customers.value) {
    const b = await api(`/api/accounts/${c.id}/balance`)
    balanceMap[c.id] = b.balance
  }
}

async function loadAllCustomersForSelect() {
  const size = 100
  let page = 1
  let totalPages = 1
  const all = []
  do {
    const data = await api(`/api/customers?keyword=&page=${page}&size=${size}`)
    all.push(...(data.items || []))
    totalPages = data.totalPages || 1
    page += 1
  } while (page <= totalPages)
  allCustomers.value = all
}

async function loadEmployees() {
  const data = await api(
    `/api/employees?keyword=${encodeURIComponent(activeNav.value === 'employees' ? keyword.value : '')}&page=${employeePager.page}&size=${employeePager.size}`
  )
  employees.value = data.items || []
  employeePager.page = data.page || 1
  employeePager.size = data.size || employeePager.size
  employeePager.total = data.total || 0
  employeePager.totalPages = data.totalPages || 1
}

async function loadServices() {
  services.value = await api('/api/config/services')
}

async function loadTransactions() {
  const data = await api(
    `/api/transactions?keyword=${encodeURIComponent(activeNav.value === 'transactions' ? keyword.value : '')}&page=${transactionPager.page}&size=${transactionPager.size}`
  )
  transactions.value = data.items || []
  transactionPager.page = data.page || 1
  transactionPager.size = data.size || transactionPager.size
  transactionPager.total = data.total || 0
  transactionPager.totalPages = data.totalPages || 1
}

async function loadAudit() {
  auditLogs.value = await api(`/api/audit/logs?keyword=${encodeURIComponent(activeNav.value === 'audit' ? keyword.value : '')}`)
}

async function loadReports() {
  const q = `startDate=${report.startDate}&endDate=${report.endDate}`
  Object.assign(reportSummary, await api(`/api/reports/summary?${q}`))
  performance.value = await api(`/api/reports/employee-performance?${q}`)
  serviceBreakdown.value = await api(`/api/reports/service-breakdown?${q}`)
}

async function loadAccessInfo() {
  Object.assign(accessInfo, await api('/api/system/access-info'))
}

async function refreshAll() {
  try {
    await Promise.all([loadSummary(), loadCustomers(), loadAllCustomersForSelect(), loadEmployees(), loadServices(), loadTransactions(), loadAudit(), loadReports(), loadAccessInfo()])
  } catch (e) {
    notify(e.message)
  }
}

function resetCustomerForm() {
  customerForm.id = ''
  customerForm.name = ''
  customerForm.phone = ''
  customerForm.verifyCode = ''
  customerForm.initialRechargeAmount = ''
  customerForm.remark = ''
  autoVerifyCode.value = ''
}

function resetEmployeeForm() {
  employeeForm.id = ''
  employeeForm.name = ''
}

function resetServiceForm() {
  serviceForm.id = ''
  serviceForm.name = ''
  serviceForm.price = ''
}

async function saveCustomer() {
  try {
    if (!customerForm.id && !customerForm.verifyCode) {
      customerForm.verifyCode = last4(customerForm.phone)
      autoVerifyCode.value = customerForm.verifyCode
    }
    if (customerForm.verifyCode && !/^\d{4}$/.test(customerForm.verifyCode)) {
      notify('校验码必须是4位数字')
      return
    }
    const payload = {
      name: customerForm.name,
      phone: customerForm.phone,
      remark: customerForm.remark,
      verifyCode: customerForm.verifyCode
    }
    if (customerForm.id) {
      await api(`/api/customers/${customerForm.id}`, { method: 'PUT', body: JSON.stringify(payload) })
      notify('会员更新成功')
    } else {
      payload.initialRechargeAmount = customerForm.initialRechargeAmount || 0
      await api('/api/customers', { method: 'POST', body: JSON.stringify(payload) })
      notify('会员创建成功')
    }
    resetCustomerForm()
    await refreshAll()
  } catch (e) {
    notify(e.message)
  }
}

function editCustomer(c) {
  customerForm.id = c.id
  customerForm.name = c.name
  customerForm.phone = c.phone
  customerForm.verifyCode = c.verifyCode || ''
  customerForm.initialRechargeAmount = ''
  customerForm.remark = c.remark
  autoVerifyCode.value = ''
}

function toggleVerifyCode(id) {
  customerVerifyVisible[id] = !customerVerifyVisible[id]
}

async function toggleCustomer(id) {
  try {
    await api(`/api/customers/${id}/toggle-status`, { method: 'PATCH' })
    notify('会员状态已更新')
    await refreshAll()
  } catch (e) {
    notify(e.message)
  }
}

async function saveEmployee() {
  try {
    const payload = { name: employeeForm.name }
    if (employeeForm.id) {
      await api(`/api/employees/${employeeForm.id}`, { method: 'PUT', body: JSON.stringify(payload) })
      notify('员工更新成功')
    } else {
      await api('/api/employees', { method: 'POST', body: JSON.stringify(payload) })
      notify('员工创建成功')
    }
    resetEmployeeForm()
    await refreshAll()
  } catch (e) {
    notify(e.message)
  }
}

function editEmployee(e) {
  employeeForm.id = e.id
  employeeForm.name = e.name
}

async function toggleEmployee(id) {
  try {
    await api(`/api/employees/${id}/toggle-status`, { method: 'PATCH' })
    notify('员工状态已更新')
    await refreshAll()
  } catch (e) {
    notify(e.message)
  }
}

async function createRecharge() {
  try {
    await api('/api/transactions/recharge', { method: 'POST', body: JSON.stringify(rechargeForm) })
    rechargeForm.customerId = ''
    rechargeForm.amount = ''
    rechargeForm.remark = ''
    rechargeMemberKeyword.value = ''
    rechargeDropdownOpen.value = false
    notify('充值成功')
    await refreshAll()
  } catch (e) {
    notify(e.message)
  }
}

function fillServicePrice() {
  const service = services.value.find((s) => s.id === consumeForm.serviceTypeId)
  if (service && !consumeForm.amount) consumeForm.amount = Number(service.price)
}

function selectRechargeCustomer(customer) {
  rechargeForm.customerId = customer.id
  rechargeMemberKeyword.value = `${customer.name} / ${customer.phone}`
  rechargeDropdownOpen.value = false
}

function selectConsumeCustomer(customer) {
  consumeForm.customerId = customer.id
  consumeMemberKeyword.value = `${customer.name} / ${customer.phone}`
  consumeForm.verifyCode = last4(customer.phone)
  consumeDropdownOpen.value = false
}

function closeRechargeDropdown() {
  setTimeout(() => {
    rechargeDropdownOpen.value = false
  }, 120)
}

function closeConsumeDropdown() {
  setTimeout(() => {
    consumeDropdownOpen.value = false
  }, 120)
}

function onRechargeMemberInput() {
  const exact = activeCustomers.value.find((c) => `${c.name} / ${c.phone}` === rechargeMemberKeyword.value)
  rechargeForm.customerId = exact ? exact.id : ''
}

function onConsumeMemberInput() {
  const exact = activeCustomers.value.find((c) => `${c.name} / ${c.phone}` === consumeMemberKeyword.value)
  if (exact) {
    consumeForm.customerId = exact.id
    consumeForm.verifyCode = last4(exact.phone)
  } else {
    consumeForm.customerId = ''
  }
}

function syncVerifyCodeByPhone() {
  if (customerForm.id) return
  const current = String(customerForm.verifyCode || '').trim()
  if (!current || current === autoVerifyCode.value) {
    customerForm.verifyCode = last4(customerForm.phone)
    autoVerifyCode.value = customerForm.verifyCode
  }
}

function changePage(type, delta) {
  if (type === 'customers') {
    customerPager.page = Math.max(1, customerPager.page + delta)
    loadCustomers()
  } else if (type === 'employees') {
    employeePager.page = Math.max(1, employeePager.page + delta)
    loadEmployees()
  } else if (type === 'transactions') {
    transactionPager.page = Math.max(1, transactionPager.page + delta)
    loadTransactions()
  } else if (type === 'audit') {
    auditPager.page = Math.min(auditTotalPages.value, Math.max(1, auditPager.page + delta))
  } else if (type === 'services') {
    servicePager.page = Math.min(serviceTotalPages.value, Math.max(1, servicePager.page + delta))
  } else if (type === 'performance') {
    performancePager.page = Math.min(performanceTotalPages.value, Math.max(1, performancePager.page + delta))
  }
}

async function createConsume() {
  try {
    if (!/^\d{4}$/.test(String(consumeForm.verifyCode || ''))) {
      notify('请输入4位数字校验码')
      return
    }
    await api('/api/transactions/consume', { method: 'POST', body: JSON.stringify(consumeForm) })
    consumeForm.customerId = ''
    consumeForm.employeeId = ''
    consumeForm.serviceTypeId = ''
    consumeForm.amount = ''
    consumeForm.verifyCode = ''
    consumeForm.remark = ''
    consumeMemberKeyword.value = ''
    consumeDropdownOpen.value = false
    notify('消费登记成功')
    await refreshAll()
  } catch (e) {
    notify(e.message)
  }
}

async function saveService() {
  try {
    const payload = { name: serviceForm.name, price: serviceForm.price }
    if (serviceForm.id) {
      await api(`/api/config/services/${serviceForm.id}`, { method: 'PUT', body: JSON.stringify(payload) })
      notify('服务更新成功')
    } else {
      await api('/api/config/services', { method: 'POST', body: JSON.stringify(payload) })
      notify('服务创建成功')
    }
    resetServiceForm()
    await refreshAll()
  } catch (e) {
    notify(e.message)
  }
}

function editService(s) {
  serviceForm.id = s.id
  serviceForm.name = s.name
  serviceForm.price = s.price
}

async function toggleService(id) {
  try {
    await api(`/api/config/services/${id}/toggle-status`, { method: 'PATCH' })
    notify('服务状态已更新')
    await refreshAll()
  } catch (e) {
    notify(e.message)
  }
}

async function downloadWithAuth(url, filename) {
  const token = sessionStorage.getItem('token')
  const res = await fetch(url, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
  if (!res.ok) {
    throw new Error('导出失败')
  }
  const blob = await res.blob()

  // EXE(JavaFX WebView) 环境：通过 Java Bridge 调起保存对话框
  if (window.ddmo && typeof window.ddmo.saveFile === 'function') {
    const base64 = await blobToBase64(blob)
    const ok = window.ddmo.saveFile(filename, base64)
    if (!ok) {
      throw new Error('导出已取消或保存失败')
    }
    return
  }

  // 浏览器环境：走原生下载
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  link.click()
  URL.revokeObjectURL(link.href)
}

function blobToBase64(blob) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = String(reader.result || '')
      const idx = result.indexOf(',')
      resolve(idx >= 0 ? result.substring(idx + 1) : result)
    }
    reader.onerror = () => reject(new Error('导出文件编码失败'))
    reader.readAsDataURL(blob)
  })
}

async function exportCustomers() {
  try {
    await downloadWithAuth('/api/export/customers', 'customers.csv')
  } catch (e) {
    notify(e.message)
  }
}

async function exportTransactions() {
  const q = `startDate=${report.startDate}&endDate=${report.endDate}`
  try {
    await downloadWithAuth(`/api/export/transactions?${q}`, 'transactions.csv')
  } catch (e) {
    notify(e.message)
  }
}

async function exportPerformance() {
  const q = `startDate=${report.startDate}&endDate=${report.endDate}`
  try {
    await downloadWithAuth(`/api/export/employee-performance?${q}`, 'employee-performance.csv')
  } catch (e) {
    notify(e.message)
  }
}

function handleLogout() {
  sessionStorage.removeItem('token')
  sessionStorage.removeItem('user')
  router.push('/login')
}

onMounted(async () => {
  await refreshAll()
})

watch(
  () => keyword.value,
  () => {
    customerPager.page = 1
    employeePager.page = 1
    transactionPager.page = 1
    auditPager.page = 1
    if (activeNav.value === 'customers') loadCustomers()
    if (activeNav.value === 'employees') loadEmployees()
    if (activeNav.value === 'transactions') loadTransactions()
    if (activeNav.value === 'audit') loadAudit()
  }
)

watch(
  () => customerListKeyword.value,
  () => {
    customerPager.page = 1
    if (activeNav.value === 'customers') loadCustomers()
  }
)

watch(
  () => auditTotalPages.value,
  (v) => {
    if (auditPager.page > v) auditPager.page = v
  }
)

watch(
  () => serviceTotalPages.value,
  (v) => {
    if (servicePager.page > v) servicePager.page = v
  }
)

watch(
  () => performanceTotalPages.value,
  (v) => {
    if (performancePager.page > v) performancePager.page = v
  }
)
</script>

<style scoped>
.dashboard {
  width: 100%;
  height: 100%;
  display: flex;
  position: relative;
}

.dashboard-window-ctrls {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 1000;
  color: var(--text-secondary);
}

.sidebar {
  width: 220px;
  min-width: 220px;
  background: linear-gradient(180deg, #0f172a 0%, #111827 100%);
  display: flex;
  flex-direction: column;
}

.sidebar-profile {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 22px 16px;
  border-bottom: 1px solid var(--border-dark);
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  overflow: hidden;
  //border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.35);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.profile-name {
  color: var(--text-light);
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.profile-role {
  color: var(--text-light-secondary);
  font-size: var(--font-size-xs);
  margin-top: 4px;
}

.sidebar-nav {
  flex: 1;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item {
  color: var(--text-light-secondary);
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  cursor: pointer;
}

.nav-item:hover {
  background: var(--bg-dark-hover);
  color: var(--text-light);
}

.nav-item.active {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff;
  box-shadow: 0 6px 16px rgba(79, 70, 229, 0.28);
}

.sidebar-bottom {
  padding: 10px;
  border-top: 1px solid var(--border-dark);
}

.logout-item:hover {
  background: rgba(239, 68, 68, 0.15);
  color: var(--danger);
}

.main-content {
  flex: 1;
  overflow: hidden;
  padding: 24px 28px;
  background: var(--bg-primary);
  display: flex;
  flex-direction: column;
}

.content-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.page-title {
  font-size: 26px;
}

.page-desc {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-input,
input,
select,
textarea {
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 10px 12px;
  background: #fff;
}

.search-input {
  width: 260px;
}

.combo {
  position: relative;
}

.combo-menu {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  max-height: 180px;
  overflow-y: auto;
  border: 1px solid var(--border);
  background: #fff;
  border-radius: var(--radius-sm);
  z-index: 40;
  box-shadow: var(--shadow-md);
}

.combo-item {
  padding: 9px 10px;
  cursor: pointer;
  font-size: var(--font-size-sm);
  border-bottom: 1px solid var(--border);
}

.combo-item:last-child {
  border-bottom: none;
}

.combo-item:hover {
  background: var(--bg-secondary);
}

.combo-empty {
  padding: 10px;
  color: var(--text-muted);
  font-size: var(--font-size-sm);
}

.stats-section {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-card {
  border: 1px solid var(--border);
  background: #fff;
  border-radius: var(--radius-md);
  padding: 16px;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
}

.stat-label {
  margin-top: 6px;
  color: var(--text-secondary);
  font-size: var(--font-size-xs);
}

.card {
  margin-top: 14px;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: #fff;
  padding: 18px;
}

.dashboard-card {
  flex: 0 0 75%;
  max-height: 75%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-title {
  font-size: var(--font-size-md);
  margin-bottom: 12px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  flex: 1;
  min-height: 0;
  gap: 12px;
}

.overview-panel {
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: #fafbff;
  padding: 12px;
  min-height: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.quote-panel {
  position: relative;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.quote-panel .overview-kicker,
.lucky-panel .overview-kicker {
  position: absolute;
  top: 12px;
  left: 12px;
  margin: 0;
}

.lucky-panel {
  position: relative;
  justify-content: center;
  align-items: center;
  text-align: center;
  overflow: hidden;
}

.lucky-panel.animating {
  animation: luckyPulse 0.95s ease;
}

.lucky-draw-btn {
  position: relative;
  z-index: 2;
}

.lucky-sparkles {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.spark {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  opacity: 0;
  background: radial-gradient(circle at 30% 30%, #fff 0%, #c7d2fe 40%, #6366f1 100%);
  box-shadow: 0 0 10px rgba(99, 102, 241, 0.45);
}

.spark-1 {
  left: 30%;
  top: 62%;
}

.spark-2 {
  left: 52%;
  top: 58%;
}

.spark-3 {
  left: 72%;
  top: 64%;
}

.lucky-panel.animating .spark-1 {
  animation: luckySpark 0.9s ease-out 0.08s;
}

.lucky-panel.animating .spark-2 {
  animation: luckySpark 0.9s ease-out 0.16s;
}

.lucky-panel.animating .spark-3 {
  animation: luckySpark 0.9s ease-out 0.24s;
}

.lucky-panel.animating .lucky-text {
  animation: luckyFlip 0.9s cubic-bezier(0.2, 0.8, 0.2, 1);
}

.overview-kicker {
  font-size: var(--font-size-xs);
  color: #4f46e5;
  font-weight: 600;
  margin-bottom: 6px;
}

.quote-text,
.lucky-text {
  margin: 0 0 10px;
  line-height: 1.5;
  font-size: var(--font-size-sm);
  color: var(--text-primary);
}

.quote-panel .quote-text {
  margin: 4px 0 0;
}

.tips-item {
  margin: 0;
  line-height: 1.45;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
}

.target-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: var(--font-size-sm);
  margin-bottom: 8px;
}

.target-track {
  width: 100%;
  height: 10px;
  border-radius: 999px;
  background: #e8ebff;
  border: 1px solid #d6dcff;
  overflow: hidden;
}

.target-bar {
  height: 100%;
  background: linear-gradient(90deg, #6366f1, #4f46e5);
  border-radius: 999px;
  transition: width 0.25s ease;
}

.target-actions {
  display: flex;
  gap: 8px;
}

.content-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  min-height: 0;
}

.customers-section {
  grid-template-columns: 1fr;
  align-content: start;
}

.list-filter-row {
  margin-bottom: 10px;
}

.list-section {
  flex: 1;
  overflow: auto;
}

.list-card {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.list-card .table-wrap {
  flex: 1;
  min-height: 0;
}

.audit-section {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.audit-section .table-wrap {
  flex: 1;
  min-height: 0;
}

.pager {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.content-grid .full {
  grid-column: 1 / -1;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

textarea {
  grid-column: 1 / -1;
  min-height: 84px;
}

.btn-row,
.quick-row,
.filter-row {
  margin-top: 12px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.primary-btn {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  height: 36px;
  padding: 0 14px;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
}

.secondary-btn {
  border: 1px solid #d6d9ff;
  background: #f6f7ff;
  color: #4f46e5;
  border-radius: var(--radius-sm);
  height: 36px;
  padding: 0 14px;
}

.secondary-btn:hover {
  border-color: #bfc5ff;
  background: #eef0ff;
}

.table-wrap {
  overflow: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

.customer-table {
  table-layout: fixed;
}

.customer-table th,
.customer-table td {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.audit-table {
  table-layout: fixed;
}

.audit-table th,
.audit-table td {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.transaction-table {
  table-layout: fixed;
}

.transaction-table th,
.transaction-table td {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

th,
td {
  text-align: left;
  border-bottom: 1px solid var(--border);
  padding: 10px 8px;
  font-size: var(--font-size-sm);
}

th {
  color: var(--text-secondary);
  font-size: var(--font-size-xs);
}

.text-btn {
  border: none;
  background: none;
  color: var(--accent);
  margin-right: 8px;
  font-size: var(--font-size-xs);
}

.text-btn.danger {
  color: var(--danger);
}

.muted {
  color: var(--text-secondary);
}

.service-chart {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
  overflow: auto;
  padding-right: 4px;
}

.report-summary-card,
.report-service-card {
  height: 192px;
  min-height: 192px;
}

.report-service-card {
  display: flex;
  flex-direction: column;
}

.report-service-card .service-chart {
  flex: 1;
  min-height: 0;
}

.report-summary-card {
  display: flex;
  flex-direction: column;
}

.summary-metrics {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.summary-item {
  border: 1px solid #e4e8ff;
  background: linear-gradient(180deg, #fbfcff, #f4f6ff);
  border-radius: var(--radius-sm);
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.summary-label {
  color: var(--text-secondary);
  font-size: var(--font-size-xs);
  margin-bottom: 4px;
}

.summary-value {
  color: #4338ca;
  font-size: var(--font-size-sm);
  font-weight: 700;
  line-height: 1.2;
}

.service-chart-row {
  display: grid;
  grid-template-columns: 130px 1fr auto;
  align-items: center;
  gap: 10px;
}

.service-chart-name {
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.service-chart-sub {
  margin-top: 2px;
  color: var(--text-secondary);
  font-size: var(--font-size-xs);
}

.service-chart-track {
  height: 12px;
  border-radius: 999px;
  background: #eef0ff;
  border: 1px solid #d8dcff;
  overflow: hidden;
}

.service-chart-bar {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #6366f1, #4f46e5);
  transition: width 0.24s ease;
}

.service-chart-value {
  font-size: var(--font-size-xs);
  color: #4338ca;
  font-weight: 600;
  white-space: nowrap;
}

@keyframes luckyPulse {
  0% {
    box-shadow: 0 0 0 rgba(79, 70, 229, 0);
    transform: translateY(0);
  }
  35% {
    box-shadow: 0 0 0 10px rgba(79, 70, 229, 0.08), 0 10px 22px rgba(79, 70, 229, 0.22);
    transform: translateY(-1px);
  }
  100% {
    box-shadow: 0 0 0 rgba(79, 70, 229, 0);
    transform: translateY(0);
  }
}

@keyframes luckyFlip {
  0% {
    opacity: 0.25;
    transform: rotateX(70deg) scale(0.96);
    filter: blur(0.5px);
  }
  45% {
    opacity: 1;
    transform: rotateX(-10deg) scale(1.02);
    filter: blur(0);
  }
  100% {
    opacity: 1;
    transform: rotateX(0) scale(1);
  }
}

@keyframes luckySpark {
  0% {
    opacity: 0;
    transform: translateY(0) scale(0.5);
  }
  20% {
    opacity: 1;
    transform: translateY(-10px) scale(1.1);
  }
  100% {
    opacity: 0;
    transform: translateY(-30px) scale(0.7);
  }
}

.toast {
  position: fixed;
  top: 18px;
  left: 50%;
  transform: translateX(-50%);
  background: #eef2ff;
  color: #4338ca;
  border: 1px solid #c7d2fe;
  padding: 10px 16px;
  border-radius: var(--radius-md);
  box-shadow: 0 8px 24px rgba(79, 70, 229, 0.15);
  z-index: 1400;
  font-size: var(--font-size-sm);
  font-weight: 600;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1200;
}

.modal-card {
  width: 420px;
  max-width: calc(100vw - 40px);
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  padding: 18px;
  box-shadow: var(--shadow-lg);
}

.single-col {
  grid-template-columns: 1fr;
}

.help-card {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.help-wrap {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.help-access-tip {
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid #a7f3d0;
  background: #ecfdf5;
}

.help-access-title {
  font-size: var(--font-size-sm);
  font-weight: 700;
  color: #047857;
  margin-bottom: 4px;
}

.help-access-desc {
  color: #065f46;
  font-size: var(--font-size-sm);
  line-height: 1.6;
}

.help-access-desc code {
  background: #d1fae5;
  border-radius: 4px;
  padding: 2px 6px;
}

.help-item {
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: #fafbff;
  padding: 12px;
}

.help-item h4 {
  margin: 0 0 8px;
  font-size: var(--font-size-sm);
  color: var(--text-primary);
}

.help-item p {
  margin: 0;
  color: var(--text-secondary);
  font-size: var(--font-size-sm);
  line-height: 1.6;
}

.help-alert {
  grid-column: 1 / -1;
  border-color: #f59e0b;
  background: #fff8eb;
}

.help-alert h4 {
  color: #b45309;
}

@media (max-width: 1100px) {
  .stats-section {
    grid-template-columns: 1fr 1fr;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }

  .content-grid {
    grid-template-columns: 1fr;
  }

  .summary-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .help-wrap {
    grid-template-columns: 1fr;
  }
}
</style>
