-- Remove the redundant second UNIQUE constraint on carts.student_id.
-- The canonical constraint uk_carts_student remains in place.
ALTER TABLE public.carts
DROP CONSTRAINT IF EXISTS ukraflf7tp6hf3rpyuw68b8rv82;
