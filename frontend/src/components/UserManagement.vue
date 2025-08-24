<template>
  <el-dialog
    v-model="visible"
    title="用户管理"
    width="90%"
    :before-close="handleClose"
    class="user-management-dialog"
    center
  >
    <div class="user-management-container">
      <!-- 操作栏 -->
      <div class="operation-bar">
        <el-button type="primary" @click="showAddUserDialog">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
        <div class="search-bar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索用户名或邮箱"
            clearable
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 用户列表 -->
      <el-table
        :data="filteredUsers"
        v-loading="loading"
        stripe
        class="user-table"
      >
        <el-table-column prop="id" label="ID" min-width="60" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        <el-table-column prop="lastLogin" label="最后登录" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.lastLogin) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
              {{ row.status === 'active' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'active'"
              type="warning"
              size="small"
              @click="disableUser(row)"
            >
              禁用
            </el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="enableUser(row)"
            >
              启用
            </el-button>
            <el-button
              type="primary"
              size="small"
              @click="editUser(row)"
            >
              编辑
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 添加/编辑用户弹窗 -->
    <el-dialog
      v-model="showUserDialog"
      :title="editingUser ? '编辑用户' : '添加用户'"
      width="500px"
      :before-close="handleUserDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="userForm.username"
            placeholder="请输入用户名"
            :disabled="editingUser"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!editingUser">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="userForm.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input
            v-model="userForm.realName"
            placeholder="请输入真实姓名"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="editingUser">
          <el-select v-model="userForm.status" placeholder="请选择状态">
            <el-option label="正常" value="active" />
            <el-option label="禁用" value="locked" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="handleUserDialogClose">取消</el-button>
        <el-button type="primary" @click="saveUser" :loading="saving">
          {{ editingUser ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { UserAPI, type User, type CreateUserRequest, type UpdateUserRequest } from '../api/user'

// Props
interface Props {
  modelValue: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

// 响应式数据
const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const saving = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 用户列表数据
const users = ref<User[]>([])
const filteredUsers = computed(() => {
  if (!searchKeyword.value) {
    return users.value
  }
  return users.value.filter(user => 
    user.username.toLowerCase().includes(searchKeyword.value.toLowerCase()) ||
    user.email.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 用户表单
const showUserDialog = ref(false)
const editingUser = ref<User | null>(null)
const userFormRef = ref<FormInstance>()
const userForm = reactive({
  username: '',
  password: '',
  email: '',
  realName: '',
  status: 'active'
})

const userRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ]
}

// 方法
const loadUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchKeyword.value || undefined
    }
    const response = await UserAPI.getUsers(params)
    users.value = response.users
    total.value = response.total
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  // 重新加载数据以支持服务端搜索
  currentPage.value = 1
  loadUsers()
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  loadUsers()
}

const handleCurrentChange = (page: number) => {
  currentPage.value = page
  loadUsers()
}

const showAddUserDialog = () => {
  editingUser.value = null
  resetUserForm()
  showUserDialog.value = true
}

const editUser = (user: User) => {
  editingUser.value = user
  userForm.username = user.username
  userForm.email = user.email
  userForm.realName = user.realName
  userForm.status = user.status
  userForm.password = '' 
  showUserDialog.value = true
}

const resetUserForm = () => {
  userForm.username = ''
  userForm.password = ''
  userForm.email = ''
  userForm.realName = ''
  userForm.status = 'active'
  userFormRef.value?.clearValidate()
}

const saveUser = async () => {
  if (!userFormRef.value) return
  
  try {
    await userFormRef.value.validate()
    saving.value = true
    
    if (editingUser.value) {
      // 更新用户
      const updateData: UpdateUserRequest = {
        email: userForm.email,
        realName: userForm.realName,
        status: userForm.status as 'active' | 'locked'
      }
      await UserAPI.updateUser(editingUser.value.id, updateData)
      ElMessage.success('用户更新成功')
    } else {
      // 添加用户
      const createData: CreateUserRequest = {
        username: userForm.username,
        password: userForm.password,
        email: userForm.email,
        realName: userForm.realName
      }
      await UserAPI.createUser(createData)
      ElMessage.success('用户添加成功')
    }
    
    showUserDialog.value = false
    loadUsers()
  } catch (error: any) {
    console.error('保存用户失败:', error)
    ElMessage.error(error.response?.data?.message || '保存用户失败')
  } finally {
    saving.value = false
  }
}

const disableUser = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要禁用用户 "${user.username}" 吗？`,
      '确认禁用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await UserAPI.disableUser(user.id)
    ElMessage.success('用户已禁用')
    loadUsers() // 重新加载用户列表
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('禁用用户失败:', error)
      ElMessage.error(error.response?.data?.message || '禁用用户失败')
    }
  }
}

const enableUser = async (user: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要启用用户 "${user.username}" 吗？`,
      '确认启用',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    await UserAPI.enableUser(user.id)
    ElMessage.success('用户已启用')
    loadUsers() // 重新加载用户列表
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('启用用户失败:', error)
      ElMessage.error(error.response?.data?.message || '启用用户失败')
    }
  }
}

const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

const handleClose = () => {
  visible.value = false
}

const handleUserDialogClose = () => {
  showUserDialog.value = false
  resetUserForm()
}

// 监听弹窗显示状态
watch(visible, (newVal) => {
  if (newVal) {
    loadUsers()
  }
})

// 组件挂载时加载数据
onMounted(() => {
  if (visible.value) {
    loadUsers()
  }
})
</script>

<style scoped>
.user-management-dialog {
  max-width: 1400px;
  
  .user-management-container {
    padding: 0;
  }
  
  .operation-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    .search-bar {
      width: 300px;
    }
  }
  
  .user-table {
    margin-bottom: 20px;
  }
  
  .pagination-container {
    display: flex;
    justify-content: center;
  }
}

:deep(.el-dialog) {
  border-radius: 8px;
  margin: 0 auto;
  max-width: 1400px;
}

@media (max-width: 1200px) {
  :deep(.el-dialog) {
    width: 95% !important;
  }
}

@media (max-width: 768px) {
  :deep(.el-dialog) {
    width: 98% !important;
  }
}

:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__header) {
  background: var(--el-color-primary-light-9);
  border-radius: 8px 8px 0 0;
  padding: 20px;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  color: var(--el-color-primary);
}

:deep(.el-table) {
  border-radius: 6px;
  overflow: hidden;
}

:deep(.el-table th) {
  background: var(--el-color-primary-light-9);
  color: var(--el-text-color-primary);
  font-weight: 600;
}

:deep(.el-button) {
  border-radius: 6px;
}

:deep(.el-input) {
  border-radius: 6px;
}

:deep(.el-select) {
  width: 100%;
}
</style>