// 绿色配色方案配置文件
// 为卡片图标提供统一的颜色管理

export const greenColorScheme = {
  // 浅绿色系 - 用于较轻的元素
  lightGreen: {
    primary: '#10b981',    // emerald-500
    secondary: '#34d399',  // emerald-400
    light: '#6ee7b7',      // emerald-300
    gradient: 'linear-gradient(135deg, #10b981, #34d399)'
  },
  
  // 中等绿色系 - 用于主要元素
  mediumGreen: {
    primary: '#059669',    // emerald-600
    secondary: '#047857',  // emerald-700
    light: '#10b981',      // emerald-500
    gradient: 'linear-gradient(135deg, #059669, #10b981)'
  },
  
  // 深绿色系 - 用于重要元素
  darkGreen: {
    primary: '#047857',    // emerald-700
    secondary: '#065f46',  // emerald-800
    light: '#059669',      // emerald-600
    gradient: 'linear-gradient(135deg, #047857, #059669)'
  },
  
  // 特殊绿色系 - 用于特定状态
  specialGreen: {
    success: '#22c55e',    // green-500
    warning: '#84cc16',    // lime-500
    info: '#06b6d4',       // cyan-500
    gradient: 'linear-gradient(135deg, #22c55e, #84cc16)'
  }
}

// 卡片图标专用配色
export const cardIconColors = {
  // 系统相关图标
  system: {
    background: greenColorScheme.mediumGreen.gradient,
    color: '#ffffff',
    shadow: '0 4px 12px rgba(16, 185, 129, 0.3)'
  },
  
  // Topic相关图标
  topic: {
    background: greenColorScheme.lightGreen.gradient,
    color: '#ffffff',
    shadow: '0 4px 12px rgba(52, 211, 153, 0.3)'
  },
  
  // 分组相关图标
  group: {
    background: greenColorScheme.darkGreen.gradient,
    color: '#ffffff',
    shadow: '0 4px 12px rgba(4, 120, 87, 0.3)'
  },
  
  // 标签相关图标
  tag: {
    background: greenColorScheme.specialGreen.gradient,
    color: '#ffffff',
    shadow: '0 4px 12px rgba(34, 197, 94, 0.3)'
  },
  
  // 在线状态
  online: {
    background: 'linear-gradient(135deg, #22c55e, #16a34a)',
    color: '#ffffff',
    shadow: '0 4px 12px rgba(34, 197, 94, 0.3)'
  },
  
  // 离线状态
  offline: {
    background: 'linear-gradient(135deg, #ef4444, #dc2626)',
    color: '#ffffff',
    shadow: '0 4px 12px rgba(239, 68, 68, 0.3)'
  },
  
  // 总计统计
  total: {
    background: greenColorScheme.mediumGreen.gradient,
    color: '#ffffff',
    shadow: '0 4px 12px rgba(5, 150, 105, 0.3)'
  },
  
  // 平均值统计
  average: {
    background: greenColorScheme.lightGreen.gradient,
    color: '#ffffff',
    shadow: '0 4px 12px rgba(16, 185, 129, 0.3)'
  }
}

// CSS变量映射
export const cssVariables = {
  '--green-primary': greenColorScheme.mediumGreen.primary,
  '--green-secondary': greenColorScheme.mediumGreen.secondary,
  '--green-light': greenColorScheme.lightGreen.primary,
  '--green-dark': greenColorScheme.darkGreen.primary,
  '--green-success': greenColorScheme.specialGreen.success,
  '--green-gradient-light': greenColorScheme.lightGreen.gradient,
  '--green-gradient-medium': greenColorScheme.mediumGreen.gradient,
  '--green-gradient-dark': greenColorScheme.darkGreen.gradient,
  '--green-gradient-special': greenColorScheme.specialGreen.gradient
}

// 导出默认配置
export default {
  greenColorScheme,
  cardIconColors,
  cssVariables
}