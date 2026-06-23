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

-- 初始图书数据（含丰富描述，用于 TF-IDF 语义搜索和推荐）
INSERT INTO sys_book (title, author, isbn, category_id, description, stock, total) VALUES
  ('红楼梦', '曹雪芹', '978-7-5322-0001-2', 1, '中国古典文学巅峰之作，以贾宝玉和林黛玉的爱情悲剧为主线，描绘了封建大家族兴衰荣辱的宏伟史诗', 3, 5),
  ('西游记', '吴承恩', '978-7-5322-0002-9', 1, '中国古典神魔小说，讲述唐僧师徒四人历经九九八十一难前往西天取经的奇幻冒险故事', 2, 3),
  ('水浒传', '施耐庵', '978-7-5322-0005-0', 1, '中国古典英雄传奇，描写北宋末年一百零八位好汉在梁山泊聚义，反抗腐败朝廷的壮烈史诗', 4, 6),
  ('三国演义', '罗贯中', '978-7-5322-0006-7', 1, '中国古典历史小说巅峰之作，展现东汉末年群雄逐鹿、三国鼎立的宏大战争画卷与智谋对决', 5, 7),
  ('三体', '刘慈欣', '978-7-5366-0001-3', 2, '雨果奖获奖科幻巨著，讲述人类文明与三体文明的首次接触与宇宙级生死博弈，硬科幻代表作', 5, 8),
  ('人工智能简史', '尼克', '978-7-5366-0002-0', 2, '全面梳理人工智能从图灵测试到深度学习的七十年发展历程，涵盖神经网络、专家系统等技术演进', 3, 4),
  ('深度学习', '伊恩·古德费洛', '978-7-5366-0003-7', 2, 'AI领域圣经级教材，系统讲解卷积神经网络、循环网络、生成对抗网络等深度学习的核心算法与数学原理', 2, 4),
  ('算法导论', '托马斯·科尔曼', '978-7-5366-0004-4', 2, '计算机科学经典教材，深入剖析排序、图论、动态规划等核心算法设计与分析，程序员必读名著', 4, 5),
  ('人类简史', '尤瓦尔·赫拉利', '978-7-5322-0003-6', 3, '从认知革命到科学革命，用宏大的视角重新审视人类七万年的演化历程，颠覆你对历史的认知', 4, 6),
  ('明朝那些事儿', '当年明月', '978-7-5322-0007-4', 3, '以幽默风趣的笔法讲述明朝三百年兴衰历史，从朱元璋起兵到崇祯亡国的精彩历史通俗读物', 6, 10),
  ('万历十五年', '黄仁宇', '978-7-5322-0008-1', 3, '以万历十五年为切片，深入剖析明代政治制度与社会结构的深层矛盾，大历史观的经典代表作', 3, 5),
  ('枪炮、病菌与钢铁', '贾雷德·戴蒙德', '978-7-5322-0009-8', 3, '普利策奖获奖作品，从地理环境与生物演化的角度解释为何不同大陆的人类社会发展路径迥异', 3, 4),
  ('小王子', '安托万·德·圣-埃克苏佩里', '978-7-5322-0004-3', 4, '全球发行量仅次于圣经的儿童文学经典，以小王子的星际旅行诠释爱与责任的真谛，适合全年龄段阅读', 6, 10),
  ('哈利·波特与魔法石', 'J.K.罗琳', '978-7-5322-0010-4', 4, '风靡全球的奇幻文学开篇之作，讲述孤儿哈利波特进入霍格沃茨魔法学校的奇妙冒险旅程', 5, 8),
  ('夏洛的网', 'E.B.怀特', '978-7-5322-0011-1', 4, '关于友谊与生命的温暖童话，蜘蛛夏洛用智慧拯救小猪威尔伯的感人故事，美国最伟大的儿童文学', 4, 5),
  ('高等数学', '同济大学数学系', '978-7-5322-0012-8', 5, '高等院校理工科经典教材，涵盖微积分、级数、微分方程等核心内容，考研数学必备参考书', 5, 8),
  ('心理学与生活', '理查德·格里格', '978-7-5322-0013-5', 5, '心理学入门经典教材，将心理学理论与日常生活紧密结合，涵盖认知、发展、社会心理学等核心领域', 3, 4),
  ('经济学原理', '曼昆', '978-7-5322-0014-2', 5, '全球最受欢迎的经济学入门教材，用通俗易懂的案例解释供需原理、市场机制与宏观经济政策', 4, 5),
  ('百年孤独', '加西亚·马尔克斯', '978-7-5322-0015-9', 1, '魔幻现实主义文学巅峰，讲述布恩迪亚家族七代人的传奇故事与马孔多小镇的百年兴衰', 4, 5),
  ('时间简史', '史蒂芬·霍金', '978-7-5322-0016-6', 2, '霍金科普经典，用通俗语言探讨宇宙起源、黑洞、时间箭头等深奥物理学问题，全球销量过千万', 3, 5),
  ('未来简史', '尤瓦尔·赫拉利', '978-7-5322-0017-3', 3, '从历史走向未来，探讨大数据、人工智能和生物技术如何重塑人类社会，引发对自由意志的深刻思考', 4, 6),
  ('格林童话', '格林兄弟', '978-7-5322-0018-0', 4, '德国浪漫主义童话经典，收录灰姑娘、白雪公主、青蛙王子等两百余篇童话，影响世界儿童文学', 5, 7),
  ('线性代数', '同济大学数学系', '978-7-5322-0019-7', 5, '理工科基础教材，系统讲解矩阵理论、线性空间、特征值等核心概念，机器学习入门数学基础', 4, 5);

-- AI智能菜单 (parent_id uses subquery for reliability across JDBC connections)
INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type) VALUES
('AI智能', 0, '/ai', NULL, 'Cpu', 3, NULL, 1);

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type)
SELECT '智能搜索', id, '/ai/search', 'ai/SmartSearch', 'Search', 1, 'ai:search', 1 FROM sys_menu WHERE menu_name = 'AI智能';

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type)
SELECT '图书推荐', id, '/ai/recommend', 'ai/Recommend', 'Star', 2, 'ai:recommend', 1 FROM sys_menu WHERE menu_name = 'AI智能';

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type)
SELECT '借阅洞察', id, '/ai/hotbooks', 'ai/HotBooks', 'TrendCharts', 3, 'ai:insight', 1 FROM sys_menu WHERE menu_name = 'AI智能';

-- 管理员角色（role_id=1）拥有所有AI菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_name IN ('AI智能', '智能搜索', '图书推荐', '借阅洞察');

-- 第二个用户：用于借阅数据多样性
INSERT INTO sys_user (username, password, nickname, role_id) VALUES
  ('user', '$2a$10$pmpb.MYu84w7wfqtTQkGj.KVrRcc3tFCoDagbmlgtB1xcCh457w4C', '普通读者', 2);
INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2);

-- 借阅记录（跨2026年1-6月，用于AI月度趋势、热门排行、分类分布）
-- 1月：6笔
INSERT INTO sys_borrow (user_id, book_id, borrow_date, due_date, return_date, status) VALUES
  (1, 1,  '2026-01-05 10:00:00', '2026-02-05 10:00:00', '2026-02-01 14:00:00', 1),
  (1, 3,  '2026-01-08 09:30:00', '2026-02-08 09:30:00', '2026-02-05 16:00:00', 1),
  (1, 5,  '2026-01-12 11:00:00', '2026-02-12 11:00:00', '2026-02-10 10:00:00', 1),
  (2, 13, '2026-01-15 14:00:00', '2026-02-15 14:00:00', '2026-02-14 09:00:00', 1),
  (2, 14, '2026-01-20 15:00:00', '2026-02-20 15:00:00', '2026-02-18 11:00:00', 1),
  (1, 9,  '2026-01-25 08:00:00', '2026-02-25 08:00:00', '2026-02-20 17:00:00', 1);

-- 2月：5笔
INSERT INTO sys_borrow (user_id, book_id, borrow_date, due_date, return_date, status) VALUES
  (1, 2,  '2026-02-03 10:00:00', '2026-03-03 10:00:00', '2026-03-01 14:00:00', 1),
  (2, 15, '2026-02-10 11:00:00', '2026-03-10 11:00:00', '2026-03-08 10:00:00', 1),
  (1, 6,  '2026-02-14 09:00:00', '2026-03-14 09:00:00', '2026-03-10 16:00:00', 1),
  (2, 22, '2026-02-18 15:30:00', '2026-03-18 15:30:00', '2026-03-15 11:00:00', 1),
  (1, 10, '2026-02-22 13:00:00', '2026-03-22 13:00:00', '2026-03-20 09:00:00', 1);

-- 3月：7笔
INSERT INTO sys_borrow (user_id, book_id, borrow_date, due_date, return_date, status) VALUES
  (1, 4,  '2026-03-01 08:30:00', '2026-04-01 08:30:00', '2026-03-28 17:00:00', 1),
  (1, 7,  '2026-03-05 10:00:00', '2026-04-05 10:00:00', '2026-04-02 14:00:00', 1),
  (2, 13, '2026-03-08 14:00:00', '2026-04-08 14:00:00', '2026-04-05 10:00:00', 1),
  (1, 19, '2026-03-12 09:00:00', '2026-04-12 09:00:00', '2026-04-10 16:00:00', 1),
  (2, 16, '2026-03-15 11:30:00', '2026-04-15 11:30:00', '2026-04-12 09:00:00', 1),
  (1, 11, '2026-03-20 15:00:00', '2026-04-20 15:00:00', '2026-04-18 14:00:00', 1),
  (1, 8,  '2026-03-25 10:00:00', '2026-04-25 10:00:00', '2026-04-22 11:00:00', 1);

-- 4月：7笔
INSERT INTO sys_borrow (user_id, book_id, borrow_date, due_date, return_date, status) VALUES
  (1, 5,  '2026-04-02 09:00:00', '2026-05-02 09:00:00', '2026-04-28 16:00:00', 1),
  (2, 14, '2026-04-05 14:00:00', '2026-05-05 14:00:00', '2026-05-02 10:00:00', 1),
  (1, 12, '2026-04-08 11:00:00', '2026-05-08 11:00:00', '2026-05-05 15:00:00', 1),
  (2, 17, '2026-04-12 10:00:00', '2026-05-12 10:00:00', '2026-05-10 09:00:00', 1),
  (1, 20, '2026-04-15 08:00:00', '2026-05-15 08:00:00', '2026-05-12 17:00:00', 1),
  (1, 3,  '2026-04-20 13:00:00', '2026-05-20 13:00:00', '2026-05-18 11:00:00', 1),
  (2, 22, '2026-04-25 15:00:00', '2026-05-25 15:00:00', '2026-05-23 14:00:00', 1);

-- 5月：7笔
INSERT INTO sys_borrow (user_id, book_id, borrow_date, due_date, return_date, status) VALUES
  (1, 6,  '2026-05-01 10:00:00', '2026-06-01 10:00:00', '2026-05-28 16:00:00', 1),
  (2, 13, '2026-05-05 14:00:00', '2026-06-05 14:00:00', '2026-06-01 10:00:00', 1),
  (1, 21, '2026-05-08 09:30:00', '2026-06-08 09:30:00', '2026-06-05 11:00:00', 1),
  (1, 1,  '2026-05-12 11:00:00', '2026-06-12 11:00:00', '2026-06-10 14:00:00', 1),
  (2, 18, '2026-05-15 08:00:00', '2026-06-15 08:00:00', '2026-06-12 09:00:00', 1),
  (1, 10, '2026-05-20 15:30:00', '2026-06-20 15:30:00', NULL, 0),
  (2, 15, '2026-05-25 10:00:00', '2026-06-25 10:00:00', NULL, 0);

-- 6月：6笔（部分还在借阅中）
INSERT INTO sys_borrow (user_id, book_id, borrow_date, due_date, return_date, status) VALUES
  (1, 7,  '2026-06-01 09:00:00', '2026-07-01 09:00:00', NULL, 0),
  (2, 14, '2026-06-03 14:00:00', '2026-07-03 14:00:00', NULL, 0),
  (1, 23, '2026-06-05 10:30:00', '2026-07-05 10:30:00', NULL, 0),
  (2, 16, '2026-06-06 11:00:00', '2026-07-06 11:00:00', NULL, 0),
  (1, 5,  '2026-06-08 08:00:00', '2026-07-08 08:00:00', '2026-06-09 17:00:00', 1),
  (2, 22, '2026-06-09 15:00:00', '2026-07-09 15:00:00', NULL, 0);