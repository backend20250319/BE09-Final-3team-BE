-- Fix the incorrect foreign key constraint in history table
-- Drop the incorrect foreign key constraint
ALTER TABLE history DROP FOREIGN KEY FKekrv60iroe0t0lhe2on5sul5w;

-- Add the correct foreign key constraint to reference pet table
ALTER TABLE history ADD CONSTRAINT FK_history_pet_no 
    FOREIGN KEY (pet_no) REFERENCES Pet(pet_no);
