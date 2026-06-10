CREATE DATABASE IF NOT EXISTS `library` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `library`;

-- 用户表
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
  nickname VARCHAR(50) COMMENT '昵称',
  role_id BIGINT COMMENT '关联角色ID',
  status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
  deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_username (username),
  INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_name VARCHAR(50) NOT NULL UNIQUE COMMENT '角色名',
  description VARCHAR(200) COMMENT '描述',
  status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
  deleted TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 菜单表
CREATE TABLE sys_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  menu_name VARCHAR(50) NOT NULL COMMENT '菜单名称',
  parent_id BIGINT DEFAULT 0 COMMENT '父菜单ID，0为一级菜单',
  path VARCHAR(200) COMMENT '路由路径',
  component VARCHAR(200) COMMENT '组件路径',
  icon VARCHAR(50) COMMENT '图标',
  sort INT DEFAULT 0 COMMENT '排序',
  perms VARCHAR(100) COMMENT '权限标识',
  menu_type TINYINT COMMENT '类型：1菜单 2按钮',
  status TINYINT DEFAULT 1 COMMENT '状态',
  deleted TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL COMMENT '角色ID',
  menu_id BIGINT NOT NULL COMMENT '菜单ID',
  UNIQUE KEY uk_role_menu (role_id, menu_id),
  INDEX idx_role_id (role_id),
  INDEX idx_menu_id (menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 用户角色关联表
CREATE TABLE sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id),
  INDEX idx_user_id (user_id),
  INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 初始数据
INSERT INTO sys_role (role_name, description) VALUES ('管理员', '系统管理员');
INSERT INTO sys_role (role_name, description) VALUES ('普通用户', '普通用户');

-- admin / admin123 BCrypt hash
INSERT INTO sys_user (username, password, nickname, role_id) VALUES
  ('admin', '$2a$10$pmpb.MYu84w7wfqtTQkGj.KVrRcc3tFCoDagbmlgtB1xcCh457w4C', '系统管理员', 1);

-- 初始菜单
INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type) VALUES
  ('首页', 0, '/dashboard', 'dashboard/Index', 'Home', 0, NULL, 1),
  ('系统管理', 0, '/system', NULL, 'Setting', 1, NULL, 1),
  ('用户管理', 2, '/system/user', 'system/UserManage', 'User', 1, 'user:list', 1),
  ('角色管理', 2, '/system/role', 'system/RoleManage', 'Role', 2, 'role:list', 1),
  ('菜单管理', 2, '/system/menu', 'system/MenuManage', 'Menu', 3, 'menu:list', 1);

-- 管理员拥有所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) SELECT 1, id FROM sys_menu;

-- 管理员拥有管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 图书分类表
CREATE TABLE IF NOT EXISTS sys_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE COMMENT '分类名称',
  description VARCHAR(200) COMMENT '描述',
  sort INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态：0禁用 1启用',
  deleted TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类表';

-- 图书表
CREATE TABLE IF NOT EXISTS sys_book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(100) NOT NULL COMMENT '书名',
  author VARCHAR(50) COMMENT '作者',
  isbn VARCHAR(50) UNIQUE COMMENT 'ISBN',
  category_id BIGINT COMMENT '分类ID',
  description TEXT COMMENT '描述',
  cover_url VARCHAR(200) COMMENT '封面URL',
  stock INT DEFAULT 0 COMMENT '库存数量',
  total INT DEFAULT 0 COMMENT '总数量',
  status TINYINT DEFAULT 1 COMMENT '状态：0下架 1上架',
  deleted TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_title (title),
  INDEX idx_author (author),
  INDEX idx_category_id (category_id),
  INDEX idx_isbn (isbn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书表';

-- 借阅记录表
CREATE TABLE IF NOT EXISTS sys_borrow (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  book_id BIGINT NOT NULL COMMENT '图书ID',
  borrow_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '借书日期',
  due_date DATETIME COMMENT '应还日期',
  return_date DATETIME COMMENT '实际还书日期',
  status TINYINT DEFAULT 0 COMMENT '状态：0借阅中 1已归还 2已逾期',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_book_id (book_id),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录表';

-- 初始分类数据
INSERT INTO sys_category (name, description, sort) VALUES
  ('文学', '文学作品类书籍', 1),
  ('科技', '科学技术类书籍', 2),
  ('历史', '历史类书籍', 3),
  ('儿童', '儿童读物', 4),
  ('教育', '教育类书籍', 5);

-- 初始图书数据
INSERT INTO sys_book (title, author, isbn, category_id, description, stock, total) VALUES
  ('红楼梦', '曹雪芹', '978-7-5322-0001-2', 1, '中国古典四大名著之一', 3, 5),
  ('西游记', '吴承恩', '978-7-5322-0002-9', 1, '中国古典四大名著之一', 2, 3),
  ('三体', '刘慈欣', '978-7-5366-0001-3', 2, '科幻小说', 5, 8),
  ('人类简史', '尤瓦尔·赫拉利', '978-7-5322-0003-6', 3, '人类历史综述', 4, 6),
  ('小王子', '安托万·德·圣-埃克苏佩里', '978-7-5322-0004-3', 4, '儿童文学经典', 6, 10);

-- AI智能菜单
INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type) VALUES
('AI智能', 0, '/ai', NULL, 'Cpu', 3, NULL, 1);

SET @ai_parent_id = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type) VALUES
('智能搜索', @ai_parent_id, '/ai/search', 'ai/SmartSearch', 'Search', 1, 'ai:search', 1),
('图书推荐', @ai_parent_id, '/ai/recommend', 'ai/Recommend', 'Star', 2, 'ai:recommend', 1),
('借阅洞察', @ai_parent_id, '/ai/hotbooks', 'ai/HotBooks', 'TrendCharts', 3, 'ai:insight', 1);

-- 管理员角色（role_id=1）拥有所有AI菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_name IN ('AI智能', '智能搜索', '图书推荐', '借阅洞察');