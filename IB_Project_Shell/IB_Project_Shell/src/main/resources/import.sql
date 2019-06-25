INSERT INTO users(email,password,certificate,active)VALUES('usera','$2y$10$2cdlxo9.FReyex2C1SrQa.k4WsJHA2rwCSH0CLPQDfRn5xJmGL7HO',NULL,true)
INSERT INTO users(email,password,certificate,active)VALUES('userb','$2y$10$2cdlxo9.FReyex2C1SrQa.k4WsJHA2rwCSH0CLPQDfRn5xJmGL7HO',NULL,true)
INSERT INTO users(email,password,certificate,active)VALUES('userc','$2y$10$2cdlxo9.FReyex2C1SrQa.k4WsJHA2rwCSH0CLPQDfRn5xJmGL7HO',NULL,false)
INSERT INTO users(email,password,certificate,active)VALUES('userd','$2y$10$2cdlxo9.FReyex2C1SrQa.k4WsJHA2rwCSH0CLPQDfRn5xJmGL7HO',NULL,false)
INSERT INTO users(email,password,certificate,active)VALUES('usere','$2y$10$2cdlxo9.FReyex2C1SrQa.k4WsJHA2rwCSH0CLPQDfRn5xJmGL7HO',NULL,false)



INSERT INTO authority(name)VALUES('ADMIN')
INSERT INTO authority(name)VALUES('REGULAR')

INSERT INTO user_authority(user_id,authority_id)VALUES(1,1)
INSERT INTO user_authority(user_id,authority_id)VALUES(1,2)
INSERT INTO user_authority(user_id,authority_id)VALUES(2,2)
INSERT INTO user_authority(user_id,authority_id)VALUES(3,2)
INSERT INTO user_authority(user_id,authority_id)VALUES(4,2)
INSERT INTO user_authority(user_id,authority_id)VALUES(5,2)


