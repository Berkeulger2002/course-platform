-- ============================================================
-- ADMIN ROLE SUPPORT
-- ============================================================
--
-- Existing users.role constraint currently allows:
-- STUDENT
-- TEACHER
--
-- Admin dashboard support requires:
-- ADMIN
--
-- Existing data is not modified.
-- Only the allowed role values are expanded.
-- ============================================================

ALTER TABLE public.users
DROP CONSTRAINT IF EXISTS users_role_check;


ALTER TABLE public.users
    ADD CONSTRAINT users_role_check
        CHECK (
            role IN (
                     'STUDENT',
                     'TEACHER',
                     'ADMIN'
                )
            );