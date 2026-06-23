-- ============================================
-- 修复 AI 菜单数据
-- 连接: 10.64.244.241:3306 library root/Wu360vs32
-- ============================================

-- 1. 清理旧的 AI 菜单角色关联
DELETE FROM sys_role_menu 
WHERE menu_id IN (SELECT id FROM sys_menu WHERE menu_name IN ('AI智能', '智能搜索', '图书推荐', '借阅洞察'));

-- 2. 清理旧的可能损坏的 AI 菜单（包括 parent_id 为 NULL 的）
DELETE FROM sys_menu WHERE menu_name IN ('智能搜索', '图书推荐', '借阅洞察', 'AI智能');

-- 3. 重新插入 AI 菜单（用子查询替代 @ai_parent_id）
INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type) VALUES
('AI智能', 0, '/ai', NULL, 'Cpu', 3, NULL, 1);

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type)
SELECT '智能搜索', id, '/ai/search', 'ai/SmartSearch', 'Search', 1, 'ai:search', 1 FROM sys_menu WHERE menu_name = 'AI智能';

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type)
SELECT '图书推荐', id, '/ai/recommend', 'ai/Recommend', 'Star', 2, 'ai:recommend', 1 FROM sys_menu WHERE menu_name = 'AI智能';

INSERT INTO sys_menu (menu_name, parent_id, path, component, icon, sort, perms, menu_type)
SELECT '借阅洞察', id, '/ai/hotbooks', 'ai/HotBooks', 'TrendCharts', 3, 'ai:insight', 1 FROM sys_menu WHERE menu_name = 'AI智能';

-- 4. 管理员角色授权
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE menu_name IN ('AI智能', '智能搜索', '图书推荐', '借阅洞察');

-- 5. 验证结果
SELECT id, menu_name, parent_id, path, icon, sort FROM sys_menu ORDER BY sort, id;
