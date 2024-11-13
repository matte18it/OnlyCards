/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.5.2-MariaDB, for osx10.19 (arm64)
--
-- Host: localhost    Database: OnlyCards
-- ------------------------------------------------------
-- Server version	11.5.2-MariaDB-ubu2404

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Sequence structure for `feature_product_seq`
--

DROP SEQUENCE IF EXISTS `feature_product_seq`;
CREATE SEQUENCE `feature_product_seq` start with 1 minvalue 1 maxvalue 9223372036854775806 increment by 50 nocache nocycle ENGINE=InnoDB;
DO SETVAL(`feature_product_seq`, 1, 0);

--
-- Sequence structure for `feature_seq`
--

DROP SEQUENCE IF EXISTS `feature_seq`;
CREATE SEQUENCE `feature_seq` start with 1 minvalue 1 maxvalue 9223372036854775806 increment by 50 nocache nocycle ENGINE=InnoDB;
DO SETVAL(`feature_seq`, 101, 0);

--
-- Sequence structure for `invalidated_token_seq`
--

DROP SEQUENCE IF EXISTS `invalidated_token_seq`;
CREATE SEQUENCE `invalidated_token_seq` start with 1 minvalue 1 maxvalue 9223372036854775806 increment by 50 nocache nocycle ENGINE=InnoDB;
DO SETVAL(`invalidated_token_seq`, 1, 0);

--
-- Sequence structure for `role_seq`
--

DROP SEQUENCE IF EXISTS `role_seq`;
CREATE SEQUENCE `role_seq` start with 1 minvalue 1 maxvalue 9223372036854775806 increment by 50 nocache nocycle ENGINE=InnoDB;
DO SETVAL(`role_seq`, 101, 0);

--
-- Table structure for table `account_wishlist`
--

DROP TABLE IF EXISTS `account_wishlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `account_wishlist` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` uuid DEFAULT NULL,
  `wishlist_id` uuid DEFAULT NULL,
  `ownership` enum('OWNER','SHARED_WITH') DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkrr8owt99podd4wyjdr4wg31h` (`user_id`),
  KEY `FKdrhudi81ebogd7rumd6ytjvq2` (`wishlist_id`),
  CONSTRAINT `FKdrhudi81ebogd7rumd6ytjvq2` FOREIGN KEY (`wishlist_id`) REFERENCES `wishlist` (`id`),
  CONSTRAINT `FKkrr8owt99podd4wyjdr4wg31h` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account_wishlist`
--

LOCK TABLES `account_wishlist` WRITE;
/*!40000 ALTER TABLE `account_wishlist` DISABLE KEYS */;
/*!40000 ALTER TABLE `account_wishlist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `address` (
  `default_address` bit(1) NOT NULL,
  `weekend_delivery` bit(1) NOT NULL,
  `zip_code` varchar(10) NOT NULL,
  `telephone_number` varchar(15) DEFAULT NULL,
  `id` uuid NOT NULL,
  `user_id` uuid NOT NULL,
  `city` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `state` varchar(255) NOT NULL,
  `street` varchar(255) NOT NULL,
  `surname` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKda8tuywtf0gb6sedwk7la1pgi` (`user_id`),
  CONSTRAINT `FKda8tuywtf0gb6sedwk7la1pgi` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES
('','','98341','1674523987','57ed7b45-406a-47c8-8d1e-0c2ead779c34','08ee6756-8b72-4854-82f2-3336d8b0de57','Crotone','Yugi','Italia','Via Dei Pini','Oh'),
('','','76243','1654289834','77b3e85e-e59b-46ef-bbcd-52129fb5436b','a93d9db9-3718-461e-b650-2a51d96f0099','Bologna','Carlo','Italia','Via Rossi','Verdino'),
('','','78600','8763239087','d7a060f5-ec92-4258-8352-5dc4d5ce9547','83977505-df9f-4716-9c7c-461f4a5d3395','Napoli','Cristian','Italia','Via Napoleone','Bianchi'),
('','','00819','1234567897','02f076b5-7b27-46e8-9859-5eb833b61708','992674df-badc-41d8-8829-4a7d61dff9e6','Roma','Carlo','Italia','Via Roma','Verdi'),
('','','87452','9863721567','f85e6fe1-1472-4bd5-b024-f1da95564bd7','9ffea693-6d2c-4a2c-8e04-eb9055668061','Torino','Alessia','Italia','Via Degli Alberi','Bianchi'),
('','','43219','9854321864','78bba040-9757-4514-b7e9-fd185c8eb903','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Milano','Francesco','Italia','Viale Dei Cedri','Gallo');
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `capability_token`
--

DROP TABLE IF EXISTS `capability_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `capability_token` (
  `id` uuid NOT NULL,
  `token` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_8d0jdlohs6ojc8wy76ii8xhyk` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `capability_token`
--

LOCK TABLES `capability_token` WRITE;
/*!40000 ALTER TABLE `capability_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `capability_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cart` (
  `id` uuid NOT NULL,
  `user_id` uuid NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_9emlp6m95v5er2bcqkjsw48he` (`user_id`),
  CONSTRAINT `FKl70asp4l4w0jmbm1tqyofho4o` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart_product`
--

DROP TABLE IF EXISTS `cart_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `cart_product` (
  `cart_id` uuid NOT NULL,
  `product_id` uuid NOT NULL,
  KEY `FK2kdlr8hs2bwl14u8oop49vrxi` (`product_id`),
  KEY `FKlv5x4iresnv4xspvomrwd8ej9` (`cart_id`),
  CONSTRAINT `FK2kdlr8hs2bwl14u8oop49vrxi` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKlv5x4iresnv4xspvomrwd8ej9` FOREIGN KEY (`cart_id`) REFERENCES `cart` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart_product`
--

LOCK TABLES `cart_product` WRITE;
/*!40000 ALTER TABLE `cart_product` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feature`
--

DROP TABLE IF EXISTS `feature`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feature` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_jhueeftkn8ve8th8m8a2878dr` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feature`
--

LOCK TABLES `feature` WRITE;
/*!40000 ALTER TABLE `feature` DISABLE KEYS */;
INSERT INTO `feature` VALUES
(8,'description'),
(7,'level'),
(6,'mana cost'),
(3,'rarity'),
(1,'set'),
(2,'set card number'),
(4,'category'),
(5,'category 2');
/*!40000 ALTER TABLE `feature` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `feature_product`
--

DROP TABLE IF EXISTS `feature_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `feature_product` (
  `feature_id` bigint(20) NOT NULL,
  `id` bigint(20) AUTO_INCREMENT NOT NULL,
  `product_type_id` uuid NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKlc4apsc8gfqmou1v1mccm9lsv` (`product_type_id`,`feature_id`),
  KEY `FK8uhg5qqxn3cymgm08ersllc5q` (`feature_id`),
  CONSTRAINT `FK8uhg5qqxn3cymgm08ersllc5q` FOREIGN KEY (`feature_id`) REFERENCES `feature` (`id`),
  CONSTRAINT `FKwgil34euq4k8eirk2ublakia` FOREIGN KEY (`product_type_id`) REFERENCES `product_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `feature_product`
--

LOCK TABLES `feature_product` WRITE;
/*!40000 ALTER TABLE `feature_product` DISABLE KEYS */;
INSERT INTO `feature_product` VALUES
(1,1,'c3f0bd0f-5024-4d2d-a155-22edfc81a282','Wizards Black Star Promos'),
(2,2,'c3f0bd0f-5024-4d2d-a155-22edfc81a282','1'),
(3,3,'c3f0bd0f-5024-4d2d-a155-22edfc81a282','Promo'),
(4,4,'c3f0bd0f-5024-4d2d-a155-22edfc81a282','Lightning'),
(1,5,'312443f6-3f9b-490c-9a83-d68ee889365d','Fusion Strike'),
(2,6,'312443f6-3f9b-490c-9a83-d68ee889365d','57'),
(3,7,'312443f6-3f9b-490c-9a83-d68ee889365d','Rare Holo'),
(4,8,'312443f6-3f9b-490c-9a83-d68ee889365d','Water'),
(1,52,'5406a881-8885-43aa-a7c9-44589aea0f66','Paldean Fates'),
(2,53,'5406a881-8885-43aa-a7c9-44589aea0f66','65'),
(3,54,'5406a881-8885-43aa-a7c9-44589aea0f66','Rare'),
(4,55,'5406a881-8885-43aa-a7c9-44589aea0f66','Metal'),
(1,102,'fa59eef9-75e2-45cf-a6e6-ea88fa3683a1','Scarlet & Violet Black Star Promos'),
(2,103,'fa59eef9-75e2-45cf-a6e6-ea88fa3683a1','75'),
(3,104,'fa59eef9-75e2-45cf-a6e6-ea88fa3683a1','Promo'),
(4,105,'fa59eef9-75e2-45cf-a6e6-ea88fa3683a1','Psychic'),
(1,106,'109f12ad-071f-4531-9380-a2295f397778','Paldean Fates'),
(2,107,'109f12ad-071f-4531-9380-a2295f397778','67'),
(3,108,'109f12ad-071f-4531-9380-a2295f397778','Rare'),
(4,109,'109f12ad-071f-4531-9380-a2295f397778','Metal'),
(1,110,'54762caf-9257-43b2-8825-f48469d6d2ac','Temporal Forces'),
(2,111,'54762caf-9257-43b2-8825-f48469d6d2ac','29'),
(3,112,'54762caf-9257-43b2-8825-f48469d6d2ac','Rare'),
(4,113,'54762caf-9257-43b2-8825-f48469d6d2ac','Fire'),
(1,114,'6e2e4dec-2b2d-40e7-b60a-368fd379b7f8','Paldean Fates'),
(2,115,'6e2e4dec-2b2d-40e7-b60a-368fd379b7f8','23'),
(3,116,'6e2e4dec-2b2d-40e7-b60a-368fd379b7f8','Common'),
(4,117,'6e2e4dec-2b2d-40e7-b60a-368fd379b7f8','Psychic'),
(1,118,'f50f1d70-07bd-43d4-b5fc-22ee7e0f0e17','Paldean Fates'),
(2,119,'f50f1d70-07bd-43d4-b5fc-22ee7e0f0e17','49'),
(3,120,'f50f1d70-07bd-43d4-b5fc-22ee7e0f0e17','Uncommon'),
(4,121,'f50f1d70-07bd-43d4-b5fc-22ee7e0f0e17','Fighting'),
(1,152,'b23c6a08-457d-45be-a97d-9d84578d7f54','Scarlet & Violet'),
(2,153,'b23c6a08-457d-45be-a97d-9d84578d7f54','32'),
(3,154,'b23c6a08-457d-45be-a97d-9d84578d7f54','Double Rare'),
(4,155,'b23c6a08-457d-45be-a97d-9d84578d7f54','Fire'),
(1,156,'3bef9ab8-ddd8-483c-94ae-1c6cdf89828f','Paldean Fates'),
(2,157,'3bef9ab8-ddd8-483c-94ae-1c6cdf89828f','25'),
(3,158,'3bef9ab8-ddd8-483c-94ae-1c6cdf89828f','Common'),
(4,159,'3bef9ab8-ddd8-483c-94ae-1c6cdf89828f','Psychic'),
(1,202,'c377604f-3e45-4bfa-b3e6-03a4de8cff61','Paldean Fates'),
(2,203,'c377604f-3e45-4bfa-b3e6-03a4de8cff61','1'),
(3,204,'c377604f-3e45-4bfa-b3e6-03a4de8cff61','Common'),
(4,205,'c377604f-3e45-4bfa-b3e6-03a4de8cff61','Grass'),
(1,252,'18149c79-5cf0-43a5-bc23-62a0c2dbbdf7','Twilight Masquerade'),
(2,253,'18149c79-5cf0-43a5-bc23-62a0c2dbbdf7','26'),
(3,254,'18149c79-5cf0-43a5-bc23-62a0c2dbbdf7','Common'),
(4,255,'18149c79-5cf0-43a5-bc23-62a0c2dbbdf7','Fire'),
(1,256,'5fc6fa61-ff1a-47bf-9897-19ea418e24ba','HS—Undaunted'),
(2,257,'5fc6fa61-ff1a-47bf-9897-19ea418e24ba','47'),
(3,258,'5fc6fa61-ff1a-47bf-9897-19ea418e24ba','Common'),
(4,259,'5fc6fa61-ff1a-47bf-9897-19ea418e24ba','Colorless'),
(1,260,'124bcccd-45fd-4adc-9724-48e9f1580dd8','Obsidian Flames'),
(2,261,'124bcccd-45fd-4adc-9724-48e9f1580dd8','86'),
(3,262,'124bcccd-45fd-4adc-9724-48e9f1580dd8','Uncommon'),
(4,263,'124bcccd-45fd-4adc-9724-48e9f1580dd8','Psychic'),
(1,264,'3c70cb6b-44bf-4c7e-8fed-d9405d41f156','151'),
(2,265,'3c70cb6b-44bf-4c7e-8fed-d9405d41f156','205'),
(3,266,'3c70cb6b-44bf-4c7e-8fed-d9405d41f156','Hyper Rare'),
(4,267,'3c70cb6b-44bf-4c7e-8fed-d9405d41f156','Psychic'),
(1,302,'9a6c3413-f30b-41d3-994e-247194908db6','Weatherlight'),
(2,303,'9a6c3413-f30b-41d3-994e-247194908db6','139'),
(3,304,'9a6c3413-f30b-41d3-994e-247194908db6','common'),
(4,305,'9a6c3413-f30b-41d3-994e-247194908db6','Creature — Elephant'),
(1,306,'e747b266-df68-4419-90e0-8214ade03f7d','Mirage'),
(2,307,'e747b266-df68-4419-90e0-8214ade03f7d','277'),
(3,308,'e747b266-df68-4419-90e0-8214ade03f7d','rare'),
(4,309,'e747b266-df68-4419-90e0-8214ade03f7d','Instant'),
(1,310,'a6f4a8d2-59fa-4f46-bf2d-935ac0d44ee3','Mirage'),
(2,311,'a6f4a8d2-59fa-4f46-bf2d-935ac0d44ee3','195'),
(3,312,'a6f4a8d2-59fa-4f46-bf2d-935ac0d44ee3','rare'),
(4,313,'a6f4a8d2-59fa-4f46-bf2d-935ac0d44ee3','Creature — Elemental Spirit'),
(1,314,'fd1a8fce-0464-4863-9f9c-4da7ee92c8b1','Unglued'),
(2,315,'fd1a8fce-0464-4863-9f9c-4da7ee92c8b1','70'),
(3,316,'fd1a8fce-0464-4863-9f9c-4da7ee92c8b1','rare'),
(4,317,'fd1a8fce-0464-4863-9f9c-4da7ee92c8b1','Artifact'),
(1,318,'5209f47f-081f-407e-92db-2405711da120','Ultimate Masters'),
(2,319,'5209f47f-081f-407e-92db-2405711da120','71'),
(3,320,'5209f47f-081f-407e-92db-2405711da120','mythic'),
(4,321,'5209f47f-081f-407e-92db-2405711da120','Creature — Human Wizard'),
(1,322,'3ce5c8b4-0746-49b8-9029-9fa72b04d642','Vintage Masters'),
(2,323,'3ce5c8b4-0746-49b8-9029-9fa72b04d642','3'),
(3,324,'3ce5c8b4-0746-49b8-9029-9fa72b04d642','bonus'),
(4,325,'3ce5c8b4-0746-49b8-9029-9fa72b04d642','Sorcery'),
(1,352,'5bc34d9b-06b4-413a-b95a-19c8bb417068','The Lord of the Rings: Tales of Middle-earth'),
(2,353,'5bc34d9b-06b4-413a-b95a-19c8bb417068','246'),
(3,354,'5bc34d9b-06b4-413a-b95a-19c8bb417068','mythic'),
(4,355,'5bc34d9b-06b4-413a-b95a-19c8bb417068','Legendary Artifact'),
(1,356,'dd9d5e3b-9b6c-4245-8b3a-a9684a7d2318','Ice Age'),
(2,357,'dd9d5e3b-9b6c-4245-8b3a-a9684a7d2318','181'),
(3,358,'dd9d5e3b-9b6c-4245-8b3a-a9684a7d2318','rare'),
(4,359,'dd9d5e3b-9b6c-4245-8b3a-a9684a7d2318','Enchantment'),
(1,360,'1e74d49e-6bbb-4924-8e00-f19285d95f8a','Shadows over Innistrad'),
(2,361,'1e74d49e-6bbb-4924-8e00-f19285d95f8a','226'),
(3,362,'1e74d49e-6bbb-4924-8e00-f19285d95f8a','mythic'),
(4,363,'1e74d49e-6bbb-4924-8e00-f19285d95f8a','Sorcery'),
(1,364,'2d6befa8-6f8e-4055-9c31-0a555390889a','Saviors of Kamigawa'),
(2,365,'2d6befa8-6f8e-4055-9c31-0a555390889a','165'),
(3,366,'2d6befa8-6f8e-4055-9c31-0a555390889a','rare'),
(4,367,'2d6befa8-6f8e-4055-9c31-0a555390889a','Legendary Land'),
(1,368,'d728f7f2-084e-4c88-8635-b0e2b7c70d71','Fallout'),
(2,369,'d728f7f2-084e-4c88-8635-b0e2b7c70d71','139'),
(3,370,'d728f7f2-084e-4c88-8635-b0e2b7c70d71','uncommon'),
(4,371,'d728f7f2-084e-4c88-8635-b0e2b7c70d71','Artifact — Bobblehead'),
(1,372,'e05c576d-fa64-4a52-87b8-18a13f0d774d','Arabian Nights'),
(2,373,'e05c576d-fa64-4a52-87b8-18a13f0d774d','6'),
(3,374,'e05c576d-fa64-4a52-87b8-18a13f0d774d','rare'),
(4,375,'e05c576d-fa64-4a52-87b8-18a13f0d774d','Creature — Human Noble'),
(1,376,'224a4618-0207-45e9-88f8-6f9650bdb2cd','Jumpstart'),
(2,377,'224a4618-0207-45e9-88f8-6f9650bdb2cd','169'),
(3,378,'224a4618-0207-45e9-88f8-6f9650bdb2cd','rare'),
(4,379,'224a4618-0207-45e9-88f8-6f9650bdb2cd','Enchantment'),
(1,380,'bea0d9e3-c9a0-43ee-8f93-bef2c0e0592a','Wilds of Eldraine'),
(2,381,'bea0d9e3-c9a0-43ee-8f93-bef2c0e0592a','208'),
(3,382,'bea0d9e3-c9a0-43ee-8f93-bef2c0e0592a','rare'),
(4,383,'bea0d9e3-c9a0-43ee-8f93-bef2c0e0592a','Creature — Faerie Shapeshifter'),
(1,384,'4ae7a060-c2e6-4401-92e1-f7e8203d2976','Vintage Masters'),
(2,385,'4ae7a060-c2e6-4401-92e1-f7e8203d2976','324'),
(3,386,'4ae7a060-c2e6-4401-92e1-f7e8203d2976','rare'),
(4,387,'4ae7a060-c2e6-4401-92e1-f7e8203d2976','Land — Island Mountain'),
(1,388,'16a811da-2bf3-4862-9c29-7810e84a653e','2016 Mega-Tins'),
(2,389,'16a811da-2bf3-4862-9c29-7810e84a653e','CT13-EN008'),
(3,390,'16a811da-2bf3-4862-9c29-7810e84a653e','Ultra Rare'),
(4,391,'16a811da-2bf3-4862-9c29-7810e84a653e','Dragon, Normal'),
(1,402,'7dd25234-c615-4390-8de9-7a3d2a7bdbbb','Booster Pack Collectors Tins 2002'),
(2,403,'7dd25234-c615-4390-8de9-7a3d2a7bdbbb','BPT-006'),
(3,404,'7dd25234-c615-4390-8de9-7a3d2a7bdbbb','Secret Rare'),
(4,405,'7dd25234-c615-4390-8de9-7a3d2a7bdbbb','Dragon, Fusion'),
(1,406,'f0b2f429-4447-40b0-9caf-a13d2ed41076','Metal Raiders'),
(2,407,'f0b2f429-4447-40b0-9caf-a13d2ed41076','MRD-006'),
(3,408,'f0b2f429-4447-40b0-9caf-a13d2ed41076','Common'),
(4,409,'f0b2f429-4447-40b0-9caf-a13d2ed41076','Insect, Normal'),
(1,410,'359d4457-8eb9-4f8e-b26c-9cabdadd2d0c','Legend of Blue Eyes White Dragon'),
(2,411,'359d4457-8eb9-4f8e-b26c-9cabdadd2d0c','LOB-105'),
(3,412,'359d4457-8eb9-4f8e-b26c-9cabdadd2d0c','Common'),
(4,413,'359d4457-8eb9-4f8e-b26c-9cabdadd2d0c','Winged Beast, Normal'),
(1,414,'c1d8db30-026c-4804-b543-5396a8371b50','Collectible Tins 2009 Wave 2'),
(2,415,'c1d8db30-026c-4804-b543-5396a8371b50','CT06-EN003'),
(3,416,'c1d8db30-026c-4804-b543-5396a8371b50','Secret Rare'),
(4,417,'c1d8db30-026c-4804-b543-5396a8371b50','Dragon, Synchro, Effect'),
(1,418,'89ae9b5b-98e3-418c-a337-30902842fcfe','Collectible Tins 2012 Wave 2'),
(2,419,'89ae9b5b-98e3-418c-a337-30902842fcfe','CT09-EN014'),
(3,420,'89ae9b5b-98e3-418c-a337-30902842fcfe','Super Rare'),
(4,421,'89ae9b5b-98e3-418c-a337-30902842fcfe','Fairy, Xyz, Effect'),
(1,422,'b461ad10-d7c3-408f-ac97-14b018118dc1','Dark Revelation Volume 1'),
(2,423,'b461ad10-d7c3-408f-ac97-14b018118dc1','DR1-EN026'),
(3,424,'b461ad10-d7c3-408f-ac97-14b018118dc1','Rare'),
(4,425,'b461ad10-d7c3-408f-ac97-14b018118dc1','Zombie, Fusion, Effect'),
(1,426,'2b6e084e-6ded-4721-b21b-bfc861b203ef','Duel Terminal 7a'),
(2,427,'2b6e084e-6ded-4721-b21b-bfc861b203ef','DT07-EN030'),
(3,428,'2b6e084e-6ded-4721-b21b-bfc861b203ef','Duel Terminal Rare Parallel Rare'),
(4,429,'2b6e084e-6ded-4721-b21b-bfc861b203ef','Warrior, Ritual'),
(1,452,'f2e3a20d-4336-4c72-bf16-6f992375d37d','Battle Pack 3: Monster League'),
(2,453,'f2e3a20d-4336-4c72-bf16-6f992375d37d','BP03-EN120'),
(3,454,'f2e3a20d-4336-4c72-bf16-6f992375d37d','Shatterfoil Rare'),
(4,455,'f2e3a20d-4336-4c72-bf16-6f992375d37d','Warrior, Xyz, Effect'),
(1,456,'1045c060-135f-49ca-bd03-9425b3676c98','Collectible Tins 2008 Wave 1'),
(2,457,'1045c060-135f-49ca-bd03-9425b3676c98','CT05-EN001'),
(3,458,'1045c060-135f-49ca-bd03-9425b3676c98','Secret Rare'),
(4,459,'1045c060-135f-49ca-bd03-9425b3676c98','Dragon, Synchro, Effect'),
(1,460,'e1a80cd5-4337-4fdc-ae2d-699261976dd0','Champion Pack: Game Four'),
(2,461,'e1a80cd5-4337-4fdc-ae2d-699261976dd0','CP04-EN001'),
(3,462,'e1a80cd5-4337-4fdc-ae2d-699261976dd0','Ultra Rare'),
(4,463,'e1a80cd5-4337-4fdc-ae2d-699261976dd0','Zombie, Effect'),
(1,464,'2d130ebf-0714-4f21-8b7c-eb65dcc9a164','2017 Mega-Tin Mega Pack'),
(2,465,'2d130ebf-0714-4f21-8b7c-eb65dcc9a164','MP17-EN074'),
(3,466,'2d130ebf-0714-4f21-8b7c-eb65dcc9a164','Super Rare'),
(4,467,'2d130ebf-0714-4f21-8b7c-eb65dcc9a164','Spellcaster, Effect'),
(1,468,'de21a1d3-6cfc-4281-8255-0b4f235af1ff','2016 Mega-Tins'),
(2,469,'de21a1d3-6cfc-4281-8255-0b4f235af1ff','CT13-EN003'),
(3,470,'de21a1d3-6cfc-4281-8255-0b4f235af1ff','Ultra Rare'),
(4,471,'de21a1d3-6cfc-4281-8255-0b4f235af1ff','Spellcaster, Normal'),
(1,472,'358a43cf-cd65-4710-adee-902d3af7c12b','2016 Mega-Tins'),
(2,473,'358a43cf-cd65-4710-adee-902d3af7c12b','CT13-EN001'),
(3,474,'358a43cf-cd65-4710-adee-902d3af7c12b','Secret Rare'),
(4,475,'358a43cf-cd65-4710-adee-902d3af7c12b','Divine-Beast, Effect'),
(1,476,'b492e317-0a1e-422a-97f8-5e8d6daeffce','25th Anniversary Tin: Dueling Mirrors'),
(2,477,'b492e317-0a1e-422a-97f8-5e8d6daeffce','MP24-EN004'),
(3,478,'b492e317-0a1e-422a-97f8-5e8d6daeffce','Quarter Century Secret Rare'),
(4,479,'b492e317-0a1e-422a-97f8-5e8d6daeffce','Spellcaster, Normal'),
(1,480,'ba4afcd8-1029-497d-84a2-82cf753cfa1f','25th Anniversary Tin: Dueling Mirrors'),
(2,481,'ba4afcd8-1029-497d-84a2-82cf753cfa1f','MP24-EN005'),
(3,482,'ba4afcd8-1029-497d-84a2-82cf753cfa1f','Quarter Century Secret Rare'),
(4,483,'ba4afcd8-1029-497d-84a2-82cf753cfa1f','Spellcaster, Normal'),
(1,484,'571d7af9-d20d-4325-a21d-aab5c7c1c8ad','25th Anniversary Tin: Dueling Mirrors'),
(2,485,'571d7af9-d20d-4325-a21d-aab5c7c1c8ad','MP24-EN003'),
(3,486,'571d7af9-d20d-4325-a21d-aab5c7c1c8ad','Quarter Century Secret Rare'),
(4,487,'571d7af9-d20d-4325-a21d-aab5c7c1c8ad','Spellcaster, Normal'),
(1,488,'22daaa8f-1032-4053-9e57-0ed3c929a9a5','25th Anniversary Tin: Dueling Mirrors'),
(2,489,'22daaa8f-1032-4053-9e57-0ed3c929a9a5','MP24-EN002'),
(3,490,'22daaa8f-1032-4053-9e57-0ed3c929a9a5','Quarter Century Secret Rare'),
(4,491,'22daaa8f-1032-4053-9e57-0ed3c929a9a5','Spellcaster, Normal'),
(1,492,'48c66c62-b953-4bc0-a228-3a8ca4143837','25th Anniversary Tin: Dueling Heroes'),
(2,493,'48c66c62-b953-4bc0-a228-3a8ca4143837','TN23-EN002'),
(3,494,'48c66c62-b953-4bc0-a228-3a8ca4143837','Quarter Century Secret Rare'),
(4,495,'48c66c62-b953-4bc0-a228-3a8ca4143837','Spellcaster, Effect');
/*!40000 ALTER TABLE `feature_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `invalidated_token`
--

DROP TABLE IF EXISTS `invalidated_token`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `invalidated_token` (
  `expiry_date` datetime(6) NOT NULL,
  `id` bigint(20) NOT NULL,
  `user_id` uuid NOT NULL,
  `token` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_b3gik94vtku15x0lfh0lg2ggv` (`token`),
  KEY `FKkkflfjpaxylvwqcichtpjnwjy` (`user_id`),
  CONSTRAINT `FKkkflfjpaxylvwqcichtpjnwjy` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `invalidated_token`
--

LOCK TABLES `invalidated_token` WRITE;
/*!40000 ALTER TABLE `invalidated_token` DISABLE KEYS */;
/*!40000 ALTER TABLE `invalidated_token` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orders` (
  `add_date` date DEFAULT NULL,
  `status` tinyint(4) NOT NULL CHECK (`status` between 0 and 3),
  `modify_date` datetime(6) DEFAULT NULL,
  `id` uuid NOT NULL,
  `user_id` uuid DEFAULT NULL,
  `user_last_edit` varchar(255) DEFAULT NULL,
  `vendor_email` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKel9kyl84ego2otj2accfd8mr7` (`user_id`),
  CONSTRAINT `FKel9kyl84ego2otj2accfd8mr7` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product` (
  `price_amount` double DEFAULT NULL,
  `price_currency` tinyint(4) DEFAULT NULL CHECK (`price_currency` between 0 and 0),
  `product_condition` tinyint(4) DEFAULT NULL CHECK (`product_condition` between 0 and 6),
  `release_date` date NOT NULL,
  `sold` bit(1) NOT NULL,
  `id` uuid NOT NULL,
  `product_type_id` uuid NOT NULL,
  `user_id` uuid DEFAULT NULL,
  `state_description` varchar(512) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKlabq3c2e90ybbxk58rc48byqo` (`product_type_id`),
  KEY `FK979liw4xk18ncpl87u4tygx2u` (`user_id`),
  CONSTRAINT `FK979liw4xk18ncpl87u4tygx2u` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKlabq3c2e90ybbxk58rc48byqo` FOREIGN KEY (`product_type_id`) REFERENCES `product_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES
(1,0,6,'2024-10-21','\0','c6d504f2-6c68-440d-affe-04cbe2051f43','2d6befa8-6f8e-4055-9c31-0a555390889a','992674df-badc-41d8-8829-4a7d61dff9e6','Carta in pessime condizioni. '),
(120,0,1,'2024-10-21','\0','8482d658-18d8-452d-b2c4-0a4310754a36','5bc34d9b-06b4-413a-b95a-19c8bb417068','992674df-badc-41d8-8829-4a7d61dff9e6','L\'unico anello di Magic, non fartelo sfuggire. Carta quasi pari a nuova!'),
(5.9,0,0,'2024-10-21','\0','da5a5582-f43d-499e-a382-1164134d1713','312443f6-3f9b-490c-9a83-d68ee889365d','a93d9db9-3718-461e-b650-2a51d96f0099','Feraligatr holo in ottime condizioni'),
(1,0,0,'2024-10-21','\0','d8827a84-95f9-4e75-8326-12889fee78c6','e747b266-df68-4419-90e0-8214ade03f7d','83977505-df9f-4716-9c7c-461f4a5d3395','Carta in ottime condizioni'),
(12.9,0,0,'2024-10-21','\0','5bcfeeae-ed0d-4027-9754-16a914b127c0','b23c6a08-457d-45be-a97d-9d84578d7f54','a93d9db9-3718-461e-b650-2a51d96f0099','Arcanine EX. Carta in ottime condizioni, pari a nuova!'),
(3.47,0,0,'2024-10-21','\0','a48877ea-03f6-4eb1-a80a-1c1ef57be9e9','ba4afcd8-1029-497d-84a2-82cf753cfa1f','9ffea693-6d2c-4a2c-8e04-eb9055668061','Braccio Sinistro di Exodia'),
(0.2,0,6,'2024-10-21','\0','203015fd-948b-43c4-ac5c-1d53d3d4f37f','f50f1d70-07bd-43d4-b5fc-22ee7e0f0e17','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Donphan comune in pessime condizioni'),
(1.45,0,3,'2024-10-21','\0','ebbe9c9c-9106-4735-bbae-295995b9931e','6e2e4dec-2b2d-40e7-b60a-368fd379b7f8','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Exeggcute comune. Le condizioni della carta sono accettabili'),
(5.4,0,1,'2024-10-21','\0','0bac30ff-26a4-465f-9941-2b66a304ef19','359d4457-8eb9-4f8e-b26c-9cabdadd2d0c','08ee6756-8b72-4854-82f2-3336d8b0de57','Carta quasi come nuova, usata qualche volta per i duelli ma nulla di rovinato'),
(1.43,0,1,'2024-10-21','\0','c7b93b8d-b1df-4b81-b7d3-2e49016f8273','e1a80cd5-4337-4fdc-ae2d-699261976dd0','9ffea693-6d2c-4a2c-8e04-eb9055668061','Carta comune quasi come nuova'),
(1,0,2,'2024-10-21','\0','1f076820-c26a-4697-8cb1-2f8cd3595793','dd9d5e3b-9b6c-4245-8b3a-a9684a7d2318','992674df-badc-41d8-8829-4a7d61dff9e6','Carta in discrete condizioni di magic. Davvero molto bella'),
(1,0,0,'2024-10-21','\0','8f71ef5d-b300-4452-9ab0-32595d5a8f9b','1e74d49e-6bbb-4924-8e00-f19285d95f8a','992674df-badc-41d8-8829-4a7d61dff9e6','Carta in ottime condizioni di magic. Molto bella'),
(1.78,0,6,'2024-10-21','\0','85595787-2bb1-41f9-80fc-34731de930cf','d728f7f2-084e-4c88-8635-b0e2b7c70d71','992674df-badc-41d8-8829-4a7d61dff9e6','Carta Holo rara ma trattata malissimo. È in pessime condizioni'),
(7.9,0,0,'2024-10-21','\0','5a33b448-efc4-4d71-9882-365af3350ad1','2b6e084e-6ded-4721-b21b-bfc861b203ef','08ee6756-8b72-4854-82f2-3336d8b0de57','Carta holo molto bella. Da avere in collezione'),
(3.9,0,3,'2024-10-21','\0','a11e0a12-1766-4545-bb20-3a0b484936fc','109f12ad-071f-4531-9380-a2295f397778','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Gholdengo holo in condizioni accettabili. '),
(1,0,6,'2024-10-21','\0','ad5b00fb-9833-41a1-a434-3f51e9160795','5406a881-8885-43aa-a7c9-44589aea0f66','a93d9db9-3718-461e-b650-2a51d96f0099','Carta in pessime condizioni'),
(1.12,0,0,'2024-10-21','\0','d0387dd5-07df-40ff-b450-3f65f3299fa8','f50f1d70-07bd-43d4-b5fc-22ee7e0f0e17','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Donpahn comune. Carta in condizioni ottime'),
(13.2,0,0,'2024-10-21','\0','12a744c4-afae-4480-9055-40d081cdd43e','d728f7f2-084e-4c88-8635-b0e2b7c70d71','992674df-badc-41d8-8829-4a7d61dff9e6','Carta Holo di magic. Davvero bella e rara, consigliato l\'acquisto'),
(500,0,2,'2024-10-21','\0','fd3432fa-75b5-4af4-8bea-45db47140f1a','fd1a8fce-0464-4863-9f9c-4da7ee92c8b1','83977505-df9f-4716-9c7c-461f4a5d3395','Carta rara magic, in discrete condizioni.'),
(3.99,0,2,'2024-10-21','\0','d97b4fbc-3b87-447f-ae0f-47391c129409','de21a1d3-6cfc-4281-8255-0b4f235af1ff','9ffea693-6d2c-4a2c-8e04-eb9055668061','Carta del mago nero in condizioni non proprio ottime. Ma sembre bellissimo'),
(5.6,0,0,'2024-10-21','\0','45a48aed-8f7b-490d-9553-486a08b47e0b','c1d8db30-026c-4804-b543-5396a8371b50','08ee6756-8b72-4854-82f2-3336d8b0de57','Carta bellissima del drago stellare maestoso. Molto molto bella fidati'),
(0.5,0,0,'2024-10-21','\0','fb6acc65-6513-4f7e-a88f-52b1e60827ad','3bef9ab8-ddd8-483c-94ae-1c6cdf89828f','a93d9db9-3718-461e-b650-2a51d96f0099','Carta in ottime condizioni'),
(10.23,0,0,'2024-10-21','\0','def523f7-a180-423d-9156-53dd0c6e5d85','de21a1d3-6cfc-4281-8255-0b4f235af1ff','9ffea693-6d2c-4a2c-8e04-eb9055668061','Mago nero in ottime condizioni, molto bella come carta. Consigliata'),
(9,0,0,'2024-10-21','\0','b4831e9e-5e57-4b9e-9ae3-5d424387b515','571d7af9-d20d-4325-a21d-aab5c7c1c8ad','9ffea693-6d2c-4a2c-8e04-eb9055668061','Gamba sinistra di Exodia'),
(1.47,0,3,'2024-10-21','\0','2534430d-d7d1-4c17-81a9-63652ca49a80','c377604f-3e45-4bfa-b3e6-03a4de8cff61','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Carta di Pineco comune. Discrete condizioni'),
(1.5,0,0,'2024-10-21','\0','d37beccd-ed82-407e-961e-637d0c0588ad','dd9d5e3b-9b6c-4245-8b3a-a9684a7d2318','992674df-badc-41d8-8829-4a7d61dff9e6','Carta in ottime condizioni, ne vale la pena'),
(3.78,0,5,'2024-10-21','\0','88e3d968-6f4c-4d93-b2f3-64c480a148a6','f2e3a20d-4336-4c72-bf16-6f992375d37d','9ffea693-6d2c-4a2c-8e04-eb9055668061','Carta in pessime condizioni. Usata tanto nei tornei ma ancora affascinante'),
(1,0,5,'2024-10-21','\0','73eace90-5f82-48de-a51d-6be4603ce0a6','224a4618-0207-45e9-88f8-6f9650bdb2cd','992674df-badc-41d8-8829-4a7d61dff9e6','Carta tenuta male, ma ancora utile'),
(3.9,0,1,'2024-10-21','\0','5002e98b-ebee-4d7f-93d8-71dfd73d8e66','54762caf-9257-43b2-8825-f48469d6d2ac','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Magcargo holo quasi in condizioni ottime'),
(2,0,1,'2024-10-21','\0','2b68424e-fdff-4d65-ad61-83d1ee41f041','5209f47f-081f-407e-92db-2405711da120','83977505-df9f-4716-9c7c-461f4a5d3395','Carta di magic molto bella e tenuta bene'),
(0.01,0,6,'2024-10-21','\0','23ad1ca6-3d54-4214-aac4-8702ae7b2046','a6f4a8d2-59fa-4f46-bf2d-935ac0d44ee3','83977505-df9f-4716-9c7c-461f4a5d3395','Carta in pessime condizioni. L\'ho usata per i tornei e si è rovinata tanto'),
(0.2,0,6,'2024-10-21','\0','3fe93d01-77aa-4a0d-a073-887a816f38d4','e747b266-df68-4419-90e0-8214ade03f7d','83977505-df9f-4716-9c7c-461f4a5d3395','Carta \"Reflect Damage\" in pessime condizioni'),
(0.5,0,3,'2024-10-21','\0','c04c7ac8-db84-4b0d-9e47-94fe01f86841','9a6c3413-f30b-41d3-994e-247194908db6','83977505-df9f-4716-9c7c-461f4a5d3395','Carta tenuta abbastanza bene nonostante l\'utilizzo davvero intenso'),
(15.52,0,0,'2024-10-21','\0','0fc07c57-f216-4655-860d-9bfad2590125','358a43cf-cd65-4710-adee-902d3af7c12b','9ffea693-6d2c-4a2c-8e04-eb9055668061','Drago del cielo, bellissimo e tenuto benissimo'),
(0.5,0,6,'2024-10-21','\0','1687c413-b3fd-4790-b7d5-9e7a85739664','312443f6-3f9b-490c-9a83-d68ee889365d','a93d9db9-3718-461e-b650-2a51d96f0099','Feraligatr holo in pessime condizioni'),
(7.45,0,0,'2024-10-21','\0','d84b55c0-b3b9-404c-970f-a411c24d9fd4','22daaa8f-1032-4053-9e57-0ed3c929a9a5','9ffea693-6d2c-4a2c-8e04-eb9055668061','Gamba Destra di Exodia'),
(1,0,0,'2024-10-21','\0','a3deb46f-e79b-4a3c-ad3f-aec6c4ecce96','9a6c3413-f30b-41d3-994e-247194908db6','83977505-df9f-4716-9c7c-461f4a5d3395','Carta \"Rogue Elephant\" bellissima in ottime condizioni'),
(1.21,0,6,'2024-10-21','\0','4ea00210-fd75-47a0-a6d7-b008e85c8bcb','1045c060-135f-49ca-bd03-9425b3676c98','9ffea693-6d2c-4a2c-8e04-eb9055668061','Carta in pessime condizioni del drago polvere di stelle'),
(5,0,5,'2024-10-21','\0','08a3d9e3-018c-4aac-a5d5-b1e7cffafda3','b23c6a08-457d-45be-a97d-9d84578d7f54','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Arcanine EX in pessime condizioni'),
(3,0,2,'2024-10-21','\0','d2db4c2b-5c01-4ba8-b57a-b2eaaf7743b0','5fc6fa61-ff1a-47bf-9897-19ea418e24ba','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Eevee comune del 2010. Carta in discrete condizioni'),
(1.9,0,0,'2024-10-21','\0','1aa1ea7a-e196-48fd-91f1-b388af1e2c2a','16a811da-2bf3-4862-9c29-7810e84a653e','992674df-badc-41d8-8829-4a7d61dff9e6','Drago Bianco Occhi Blu. Classica Carta di Yu-Gi-Oh! tenuta molto bene'),
(3.9,0,0,'2024-10-21','\0','2f10a447-b99b-4102-88df-b3f52ac63425','5406a881-8885-43aa-a7c9-44589aea0f66','a93d9db9-3718-461e-b650-2a51d96f0099','Revavroom holo in ottime condizioni'),
(4.6,0,4,'2024-10-21','\0','7c130d63-4e42-4e35-921d-b92758ba9252','2d6befa8-6f8e-4055-9c31-0a555390889a','992674df-badc-41d8-8829-4a7d61dff9e6','Carta rara in discrete condizioni'),
(1.8,0,2,'2024-10-21','\0','9195d949-14a3-4d4e-bbb5-bcc7c36cad32','3bef9ab8-ddd8-483c-94ae-1c6cdf89828f','a93d9db9-3718-461e-b650-2a51d96f0099','Natu, carta comune in discrete condizioni'),
(12.5,0,0,'2024-10-21','\0','fd158478-669b-494f-a6da-c0a4068f123f','48c66c62-b953-4bc0-a228-3a8ca4143837','9ffea693-6d2c-4a2c-8e04-eb9055668061','EXODIA'),
(6.5,0,3,'2024-10-21','\0','1df8c80b-a3f3-4f78-a6f8-c250321871c9','e05c576d-fa64-4a52-87b8-18a13f0d774d','992674df-badc-41d8-8829-4a7d61dff9e6','Carta molto bella in condizioni discrete'),
(5.89,0,1,'2024-10-21','\0','af76b2d6-2a62-4af9-a1e4-c68ffc23d824','bea0d9e3-c9a0-43ee-8f93-bef2c0e0592a','992674df-badc-41d8-8829-4a7d61dff9e6','Carta bella, tenuta bene. Molto utile'),
(8.56,0,2,'2024-10-21','\0','d190ea83-a600-48f6-8d58-cb08ccc13d31','89ae9b5b-98e3-418c-a337-30902842fcfe','08ee6756-8b72-4854-82f2-3336d8b0de57','Carta particolare e bella del numero 16 tenuta molto bene'),
(5,0,0,'2024-10-21','\0','67565f92-0268-4ec5-a60e-cd34eef03c7a','fa59eef9-75e2-45cf-a6e6-ea88fa3683a1','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Mimikyu Alternative Art in ottime condizioni'),
(1.9,0,4,'2024-10-21','\0','0195141c-cfd2-40a5-b84d-ce5ebaafb394','124bcccd-45fd-4adc-9724-48e9f1580dd8','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Espeon rara. Carta in pessime condizioni'),
(0.7,0,4,'2024-10-21','\0','bd259f9f-3bf5-4b2f-a844-cfa92782ac90','a6f4a8d2-59fa-4f46-bf2d-935ac0d44ee3','83977505-df9f-4716-9c7c-461f4a5d3395','Carta in discrete condizioni. Usata per giocare qualche volta, ma non presenta danni eccessivi'),
(1,0,0,'2024-10-21','\0','31f3f3dd-36cd-43a3-b61e-d55129a5fb94','3ce5c8b4-0746-49b8-9029-9fa72b04d642','83977505-df9f-4716-9c7c-461f4a5d3395','Carta davvero bella di magic, in ottimo stato'),
(0.56,0,6,'2024-10-21','\0','2f4fff78-64e8-4ad5-a006-d73415f8e1de','7dd25234-c615-4390-8de9-7a3d2a7bdbbb','08ee6756-8b72-4854-82f2-3336d8b0de57','Carta in pessime condizioni'),
(2.67,0,0,'2024-10-21','\0','894dd9c6-ed1b-4f60-af9c-dac13cd41bc9','1045c060-135f-49ca-bd03-9425b3676c98','9ffea693-6d2c-4a2c-8e04-eb9055668061','Carta del drago polvere di stelle. Le condizioni sono eccellenti'),
(52.5,0,0,'2024-10-21','\0','33d54af8-4d92-460d-8a8a-df4d455bfa27','3c70cb6b-44bf-4c7e-8fed-d9405d41f156','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Mew full art gold. Carta fuori serie in ottime condizioni'),
(0.6,0,0,'2024-10-21','\0','d0f71256-9950-41d4-9193-e5021dfad6c0','18149c79-5cf0-43a5-bc23-62a0c2dbbdf7','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','Vulpix Holo. Carta in ottime condizioni'),
(1.5,0,5,'2024-10-21','\0','0f757180-6f37-4ef6-9ead-e6c138a5b312','b461ad10-d7c3-408f-ac97-14b018118dc1','08ee6756-8b72-4854-82f2-3336d8b0de57','Carta in pessime condizioni, ma molto forte e bella'),
(0.2,0,6,'2024-10-21','\0','c0e88790-9143-4569-a0ee-e81fa727e625','f0b2f429-4447-40b0-9caf-a13d2ed41076','08ee6756-8b72-4854-82f2-3336d8b0de57','Carta orribile in pessime condizioni'),
(1.89,0,2,'2024-10-21','\0','32fa64b0-b2d5-47d7-82cf-ea15167c12d3','7dd25234-c615-4390-8de9-7a3d2a7bdbbb','08ee6756-8b72-4854-82f2-3336d8b0de57','Carta in discrete condizioni. Molto bella e forte'),
(6.7,0,3,'2024-10-21','\0','6add8eaa-06aa-4e72-a57f-ea1b5a3eae40','2d130ebf-0714-4f21-8b7c-eb65dcc9a164','9ffea693-6d2c-4a2c-8e04-eb9055668061','Carta in condizioni accettabili, molto bella e forte'),
(5.5,0,0,'2024-10-21','\0','27e3c19b-5de9-4bd4-bd24-f34d6f937bb5','b492e317-0a1e-422a-97f8-5e8d6daeffce','9ffea693-6d2c-4a2c-8e04-eb9055668061','Braccio destro di Exodia'),
(8.4,0,6,'2024-10-21','\0','1aa9d7c5-34f4-4039-ad39-f38f039db865','4ae7a060-c2e6-4401-92e1-f7e8203d2976','992674df-badc-41d8-8829-4a7d61dff9e6','Carta molto bella di magic, tenuta male'),
(10,0,3,'2024-10-21','\0','6e7844a9-464b-4996-9fec-f91eba0f1e6c','312443f6-3f9b-490c-9a83-d68ee889365d','a93d9db9-3718-461e-b650-2a51d96f0099','Feraligatr holo in condizioni accettabili');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_photo`
--

DROP TABLE IF EXISTS `product_photo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_photo` (
  `id` uuid NOT NULL,
  `product_id` uuid NOT NULL,
  `photo` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsgi4krgb65fktq55a5xw6seqq` (`product_id`),
  CONSTRAINT `FKsgi4krgb65fktq55a5xw6seqq` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_photo`
--

LOCK TABLES `product_photo` WRITE;
/*!40000 ALTER TABLE `product_photo` DISABLE KEYS */;
INSERT INTO `product_photo` VALUES
('e28059a8-ecdc-4a87-b1c0-0553c4fd0ad1','85595787-2bb1-41f9-80fc-34731de930cf','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F85595787-2bb1-41f9-80fc-34731de930cf_0?alt=media'),
('821ca73c-7226-4ed6-a12a-09e72efd6290','c0e88790-9143-4569-a0ee-e81fa727e625','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fc0e88790-9143-4569-a0ee-e81fa727e625_0?alt=media'),
('5ce1791c-2d11-4452-9a6c-18a876ac874a','d84b55c0-b3b9-404c-970f-a411c24d9fd4','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fd84b55c0-b3b9-404c-970f-a411c24d9fd4_0?alt=media'),
('e5e8d3d0-925c-4ea4-8f02-1eb4f92ed681','0bac30ff-26a4-465f-9941-2b66a304ef19','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F0bac30ff-26a4-465f-9941-2b66a304ef19_0?alt=media'),
('01f3ef03-900d-42cf-a0a3-22f6b88877cf','203015fd-948b-43c4-ac5c-1d53d3d4f37f','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F203015fd-948b-43c4-ac5c-1d53d3d4f37f_0?alt=media'),
('9b3f9a44-fe6e-4a41-a14e-2475c8d9b5b8','a11e0a12-1766-4545-bb20-3a0b484936fc','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fa11e0a12-1766-4545-bb20-3a0b484936fc_0?alt=media'),
('51444848-c1b6-402d-9915-25a40ae4e2f7','d97b4fbc-3b87-447f-ae0f-47391c129409','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fd97b4fbc-3b87-447f-ae0f-47391c129409_0?alt=media'),
('1eeb51fe-a3d0-45e8-b71e-28ea17c03ae7','32fa64b0-b2d5-47d7-82cf-ea15167c12d3','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F32fa64b0-b2d5-47d7-82cf-ea15167c12d3_0?alt=media'),
('cf8095c7-a0d0-435b-a661-293a2c10cfae','1df8c80b-a3f3-4f78-a6f8-c250321871c9','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F1df8c80b-a3f3-4f78-a6f8-c250321871c9_0?alt=media'),
('19df3b35-e28c-487a-a113-34ec9bc24c33','fd3432fa-75b5-4af4-8bea-45db47140f1a','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Ffd3432fa-75b5-4af4-8bea-45db47140f1a_0?alt=media'),
('4b6eb8bd-f41f-4c06-a614-3794e6e4acb0','9195d949-14a3-4d4e-bbb5-bcc7c36cad32','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F9195d949-14a3-4d4e-bbb5-bcc7c36cad32_0?alt=media'),
('3c379cc0-4609-4968-92c8-42057b7bcaea','8f71ef5d-b300-4452-9ab0-32595d5a8f9b','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F8f71ef5d-b300-4452-9ab0-32595d5a8f9b_0?alt=media'),
('beec4ef2-fbcd-4b02-97fb-42f260136e97','3fe93d01-77aa-4a0d-a073-887a816f38d4','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F3fe93d01-77aa-4a0d-a073-887a816f38d4_0?alt=media'),
('78e36bcd-c8a6-429b-be53-43c86be78add','da5a5582-f43d-499e-a382-1164134d1713','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fda5a5582-f43d-499e-a382-1164134d1713_0?alt=media'),
('8f9e5b4a-bd8e-4647-b61d-44f1501ac29f','6add8eaa-06aa-4e72-a57f-ea1b5a3eae40','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F6add8eaa-06aa-4e72-a57f-ea1b5a3eae40_0?alt=media'),
('4459ded5-87c6-494a-9a65-45d77ba0fbaa','88e3d968-6f4c-4d93-b2f3-64c480a148a6','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F88e3d968-6f4c-4d93-b2f3-64c480a148a6_0?alt=media'),
('094d12a8-4796-4db4-9afb-461cfa9a3ec5','45a48aed-8f7b-490d-9553-486a08b47e0b','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F45a48aed-8f7b-490d-9553-486a08b47e0b_0?alt=media'),
('cc3ade3d-e890-46d8-ab56-4843a77e0cf0','4ea00210-fd75-47a0-a6d7-b008e85c8bcb','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F4ea00210-fd75-47a0-a6d7-b008e85c8bcb_0?alt=media'),
('f7560682-f721-4fad-96bc-48e258e00536','33d54af8-4d92-460d-8a8a-df4d455bfa27','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F33d54af8-4d92-460d-8a8a-df4d455bfa27_0?alt=media'),
('b3b59c43-1e96-40b4-ada7-48f565f2c21b','1aa9d7c5-34f4-4039-ad39-f38f039db865','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F1aa9d7c5-34f4-4039-ad39-f38f039db865_0?alt=media'),
('100cc0db-ddb3-41f9-b247-5279cfc00111','73eace90-5f82-48de-a51d-6be4603ce0a6','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F73eace90-5f82-48de-a51d-6be4603ce0a6_0?alt=media'),
('0b7dfca6-55b8-4f8a-b974-57969b66f754','8482d658-18d8-452d-b2c4-0a4310754a36','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F8482d658-18d8-452d-b2c4-0a4310754a36_0?alt=media'),
('959aafcc-a933-4c28-a133-5ab208ad91cd','a3deb46f-e79b-4a3c-ad3f-aec6c4ecce96','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fa3deb46f-e79b-4a3c-ad3f-aec6c4ecce96_0?alt=media'),
('b3693e60-06ed-406d-a857-5c81a49c26b8','d190ea83-a600-48f6-8d58-cb08ccc13d31','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fd190ea83-a600-48f6-8d58-cb08ccc13d31_0?alt=media'),
('7b0a035b-23b5-43e0-b39b-5db63a303129','bd259f9f-3bf5-4b2f-a844-cfa92782ac90','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fbd259f9f-3bf5-4b2f-a844-cfa92782ac90_0?alt=media'),
('b1145505-9b29-458e-b489-5e975b444390','894dd9c6-ed1b-4f60-af9c-dac13cd41bc9','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F894dd9c6-ed1b-4f60-af9c-dac13cd41bc9_0?alt=media'),
('c4f8cf8b-3054-474f-8ed9-62d06e5e0ccf','ad5b00fb-9833-41a1-a434-3f51e9160795','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fad5b00fb-9833-41a1-a434-3f51e9160795_0?alt=media'),
('b2717633-9b05-4bfa-8d85-68b2d0a0d575','c7b93b8d-b1df-4b81-b7d3-2e49016f8273','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fc7b93b8d-b1df-4b81-b7d3-2e49016f8273_0?alt=media'),
('c21716b3-1b17-4f90-bab6-7004863e120a','0195141c-cfd2-40a5-b84d-ce5ebaafb394','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F0195141c-cfd2-40a5-b84d-ce5ebaafb394_0?alt=media'),
('d7f8b99f-8a5d-469e-9c5e-70b006a7c195','5bcfeeae-ed0d-4027-9754-16a914b127c0','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F5bcfeeae-ed0d-4027-9754-16a914b127c0_0?alt=media'),
('3e328ca9-4092-404e-87e3-71f0d587698c','2f10a447-b99b-4102-88df-b3f52ac63425','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F2f10a447-b99b-4102-88df-b3f52ac63425_0?alt=media'),
('d111bab3-60f3-40de-a348-755c7e8c99ea','7c130d63-4e42-4e35-921d-b92758ba9252','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F7c130d63-4e42-4e35-921d-b92758ba9252_0?alt=media'),
('c0de55ea-88a6-4f40-911f-7868a6a49cfc','2534430d-d7d1-4c17-81a9-63652ca49a80','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F2534430d-d7d1-4c17-81a9-63652ca49a80_0?alt=media'),
('6614a01e-6652-4780-8c5c-7c10d6eb9ab4','1687c413-b3fd-4790-b7d5-9e7a85739664','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F1687c413-b3fd-4790-b7d5-9e7a85739664_0?alt=media'),
('43fa0b9a-900f-45f9-a3fa-840d83e2c68e','d2db4c2b-5c01-4ba8-b57a-b2eaaf7743b0','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fd2db4c2b-5c01-4ba8-b57a-b2eaaf7743b0_0?alt=media'),
('2ffcb326-4724-45a9-97a9-8834afdb207c','5a33b448-efc4-4d71-9882-365af3350ad1','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F5a33b448-efc4-4d71-9882-365af3350ad1_0?alt=media'),
('1cfda82a-c3f5-439a-88c8-91d7924b6c37','1f076820-c26a-4697-8cb1-2f8cd3595793','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F1f076820-c26a-4697-8cb1-2f8cd3595793_0?alt=media'),
('b1b9fd4c-4732-407e-96d7-9521b6eaa735','d37beccd-ed82-407e-961e-637d0c0588ad','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fd37beccd-ed82-407e-961e-637d0c0588ad_0?alt=media'),
('7be1a62e-d2c2-4872-932e-9867244eea59','5002e98b-ebee-4d7f-93d8-71dfd73d8e66','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F5002e98b-ebee-4d7f-93d8-71dfd73d8e66_0?alt=media'),
('24f4e8e8-7c4d-462e-9684-a0a7ebdf7d06','fd158478-669b-494f-a6da-c0a4068f123f','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Ffd158478-669b-494f-a6da-c0a4068f123f_0?alt=media'),
('08ad683c-ac58-4212-a9a8-a301da766ad8','0f757180-6f37-4ef6-9ead-e6c138a5b312','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F0f757180-6f37-4ef6-9ead-e6c138a5b312_0?alt=media'),
('6723a56b-53c9-4623-9778-a6157595ccf8','fb6acc65-6513-4f7e-a88f-52b1e60827ad','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Ffb6acc65-6513-4f7e-a88f-52b1e60827ad_0?alt=media'),
('d9bbde22-66ed-4f3f-b998-b11510b9b93b','d8827a84-95f9-4e75-8326-12889fee78c6','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fd8827a84-95f9-4e75-8326-12889fee78c6_0?alt=media'),
('2dc4da5e-ccc3-4f4a-8a75-b2ee5bb9cd90','ebbe9c9c-9106-4735-bbae-295995b9931e','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Febbe9c9c-9106-4735-bbae-295995b9931e_0?alt=media'),
('6baba48e-e1ed-4082-b0b8-b8bb34203bac','12a744c4-afae-4480-9055-40d081cdd43e','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F12a744c4-afae-4480-9055-40d081cdd43e_0?alt=media'),
('67da7e9b-45f7-4c64-a19f-bacf486af3c4','c04c7ac8-db84-4b0d-9e47-94fe01f86841','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fc04c7ac8-db84-4b0d-9e47-94fe01f86841_0?alt=media'),
('b93c06bb-e404-4674-9d12-bed4e6cf2f67','a48877ea-03f6-4eb1-a80a-1c1ef57be9e9','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fa48877ea-03f6-4eb1-a80a-1c1ef57be9e9_0?alt=media'),
('848c6bbb-ecad-4c04-8821-c1a08d127a57','6e7844a9-464b-4996-9fec-f91eba0f1e6c','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F6e7844a9-464b-4996-9fec-f91eba0f1e6c_0?alt=media'),
('9a6a5958-94b8-4fba-909b-c6cb8c3baafd','def523f7-a180-423d-9156-53dd0c6e5d85','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fdef523f7-a180-423d-9156-53dd0c6e5d85_0?alt=media'),
('46ecd9b3-d340-4bd4-b0fb-cac0ab6097dc','23ad1ca6-3d54-4214-aac4-8702ae7b2046','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F23ad1ca6-3d54-4214-aac4-8702ae7b2046_0?alt=media'),
('b933385f-5491-4b68-a1cc-d0aeec351be7','0fc07c57-f216-4655-860d-9bfad2590125','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F0fc07c57-f216-4655-860d-9bfad2590125_0?alt=media'),
('bcf11911-4eb3-49c4-ba6f-d237fdb87ffb','2f4fff78-64e8-4ad5-a006-d73415f8e1de','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F2f4fff78-64e8-4ad5-a006-d73415f8e1de_0?alt=media'),
('87024dc8-783b-4d16-a02d-d4bf07c3d4bd','d0387dd5-07df-40ff-b450-3f65f3299fa8','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fd0387dd5-07df-40ff-b450-3f65f3299fa8_0?alt=media'),
('0cc6b5c5-93e5-423c-b81d-d6028486ee04','1aa1ea7a-e196-48fd-91f1-b388af1e2c2a','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F1aa1ea7a-e196-48fd-91f1-b388af1e2c2a_0?alt=media'),
('418f0484-6577-4369-a3ba-d87e2ee67ba9','31f3f3dd-36cd-43a3-b61e-d55129a5fb94','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F31f3f3dd-36cd-43a3-b61e-d55129a5fb94_0?alt=media'),
('cdabcdec-91fb-4cdb-bfe2-e09afcd17bb0','da5a5582-f43d-499e-a382-1164134d1713','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fda5a5582-f43d-499e-a382-1164134d1713_1?alt=media'),
('3c65ca70-f8f5-431f-871b-e0d6f75904e0','08a3d9e3-018c-4aac-a5d5-b1e7cffafda3','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F08a3d9e3-018c-4aac-a5d5-b1e7cffafda3_0?alt=media'),
('f4772049-efde-4b82-bd90-e658b0c8030f','c6d504f2-6c68-440d-affe-04cbe2051f43','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fc6d504f2-6c68-440d-affe-04cbe2051f43_0?alt=media'),
('2ffccc71-b5ac-4619-ac6f-e6d8845b3f70','27e3c19b-5de9-4bd4-bd24-f34d6f937bb5','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F27e3c19b-5de9-4bd4-bd24-f34d6f937bb5_0?alt=media'),
('2802ca87-ec80-4c4c-a3e4-ef2491665ba0','b4831e9e-5e57-4b9e-9ae3-5d424387b515','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fb4831e9e-5e57-4b9e-9ae3-5d424387b515_0?alt=media'),
('e6115478-bdf9-41dc-8749-f42251f8f281','d0f71256-9950-41d4-9193-e5021dfad6c0','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Fd0f71256-9950-41d4-9193-e5021dfad6c0_0?alt=media'),
('84f13647-e9f8-4729-88f2-f5d9f0d23507','af76b2d6-2a62-4af9-a1e4-c68ffc23d824','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2Faf76b2d6-2a62-4af9-a1e4-c68ffc23d824_0?alt=media'),
('75ecadcc-57da-4866-9148-f7b32aec043f','67565f92-0268-4ec5-a60e-cd34eef03c7a','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F67565f92-0268-4ec5-a60e-cd34eef03c7a_0?alt=media'),
('fcab68be-f024-48d0-818d-f96530b59787','2b68424e-fdff-4d65-ad61-83d1ee41f041','https://firebasestorage.googleapis.com/v0/b/onlycards-43854.appspot.com/o/products%2F2b68424e-fdff-4d65-ad61-83d1ee41f041_0?alt=media');
/*!40000 ALTER TABLE `product_photo` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_transactions`
--

DROP TABLE IF EXISTS `product_transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_transactions` (
  `product_id` uuid NOT NULL,
  `transactions_id` uuid NOT NULL,
  UNIQUE KEY `UK_7q9ryxtbkh8c20sx1rhgnn9kx` (`transactions_id`),
  KEY `FKidqhubsw5g8obx95pmu54w2d5` (`product_id`),
  CONSTRAINT `FKidqhubsw5g8obx95pmu54w2d5` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `FKsdxw7crtip8rylp2jyh7ijuvd` FOREIGN KEY (`transactions_id`) REFERENCES `transactions` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_transactions`
--

LOCK TABLES `product_transactions` WRITE;
/*!40000 ALTER TABLE `product_transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `product_transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_type`
--

DROP TABLE IF EXISTS `product_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_type` (
  `last_add` date DEFAULT NULL,
  `min_price_amount` double DEFAULT NULL,
  `min_price_currency` tinyint(4) DEFAULT NULL CHECK (`min_price_currency` between 0 and 0),
  `num_sell` int(11) DEFAULT NULL,
  `id` uuid NOT NULL,
  `name` varchar(50) NOT NULL,
  `game` varchar(255) NOT NULL,
  `language` varchar(255) NOT NULL,
  `photo` varchar(255) DEFAULT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_type`
--

LOCK TABLES `product_type` WRITE;
/*!40000 ALTER TABLE `product_type` DISABLE KEYS */;
INSERT INTO `product_type` VALUES
('2024-10-21',1.47,0,0,'c377604f-3e45-4bfa-b3e6-03a4de8cff61','Pineco','Pokémon','EN','https://images.pokemontcg.io/sv4pt5/1_hires.png','Card'),
('2024-10-21',1,0,0,'2d6befa8-6f8e-4055-9c31-0a555390889a','Tomb of Urami','Magic','EN','https://cards.scryfall.io/normal/front/9/0/90fedf90-825c-4814-8f0d-170f537db44c.jpg?1577853103','Card'),
('2024-10-21',3.99,0,0,'de21a1d3-6cfc-4281-8255-0b4f235af1ff','Dark Magician','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/46986414.jpg','Card'),
('2024-10-21',7.45,0,0,'22daaa8f-1032-4053-9e57-0ed3c929a9a5','Right Leg of the Forbidden One','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/8124921.jpg','Card'),
('2024-10-21',1.5,0,0,'b461ad10-d7c3-408f-ac97-14b018118dc1','Reaper on the Nightmare','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/85684223.jpg','Card'),
('2024-10-21',6.5,0,0,'e05c576d-fa64-4a52-87b8-18a13f0d774d','King Suleiman','Magic','EN','https://cards.scryfall.io/normal/front/4/d/4d3dce0f-2168-4f63-b2f9-156a11beeea7.jpg?1592364322','Card'),
('2024-10-21',120,0,0,'5bc34d9b-06b4-413a-b95a-19c8bb417068','The One Ring','Magic','EN','https://cards.scryfall.io/normal/front/d/5/d5806e68-1054-458e-866d-1f2470f682b2.jpg?1715080486','Card'),
('2024-10-21',3,0,0,'5fc6fa61-ff1a-47bf-9897-19ea418e24ba','Eevee','Pokémon','EN','https://images.pokemontcg.io/hgss3/47_hires.png','Card'),
('2024-10-21',0.5,0,0,'3bef9ab8-ddd8-483c-94ae-1c6cdf89828f','Natu','Pokémon','EN','https://images.pokemontcg.io/sv4pt5/25_hires.png','Card'),
('2024-10-21',0,0,0,'c3f0bd0f-5024-4d2d-a155-22edfc81a282','Pikachu','Pokémon','EN','https://images.pokemontcg.io/basep/1_hires.png','Card'),
('2024-10-21',0.2,0,0,'f50f1d70-07bd-43d4-b5fc-22ee7e0f0e17','Donphan','Pokémon','EN','https://images.pokemontcg.io/sv4pt5/49_hires.png','Card'),
('2024-10-21',2,0,0,'5209f47f-081f-407e-92db-2405711da120','Snapcaster Mage','Magic','EN','https://cards.scryfall.io/normal/front/7/e/7e41765e-43fe-461d-baeb-ee30d13d2d93.jpg?1547516526','Card'),
('2024-10-21',0.5,0,0,'9a6c3413-f30b-41d3-994e-247194908db6','Rogue Elephant','Magic','EN','https://cards.scryfall.io/normal/front/1/b/1b622b2f-84ad-4203-97fa-35af09e1c370.jpg?1562799604','Card'),
('2024-10-21',8.56,0,0,'89ae9b5b-98e3-418c-a337-30902842fcfe','Number 16: Shock Master','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/54719828.jpg','Card'),
('2024-10-21',1.45,0,0,'6e2e4dec-2b2d-40e7-b60a-368fd379b7f8','Exeggcute','Pokémon','EN','https://images.pokemontcg.io/sv4pt5/23_hires.png','Card'),
('2024-10-21',12.5,0,0,'48c66c62-b953-4bc0-a228-3a8ca4143837','Exodia the Forbidden One','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/33396948.jpg','Card'),
('2024-10-21',1,0,0,'5406a881-8885-43aa-a7c9-44589aea0f66','Revavroom','Pokémon','EN','https://images.pokemontcg.io/sv4pt5/65_hires.png','Card'),
('2024-10-21',1.9,0,0,'124bcccd-45fd-4adc-9724-48e9f1580dd8','Espeon','Pokémon','EN','https://images.pokemontcg.io/sv3/86_hires.png','Card'),
('2024-10-21',500,0,0,'fd1a8fce-0464-4863-9f9c-4da7ee92c8b1','Blacker Lotus','Magic','EN','https://cards.scryfall.io/normal/front/4/c/4c85d097-e87b-41ee-93c6-0e54ec41b174.jpg?1562799094','Card'),
('2024-10-21',5.6,0,0,'c1d8db30-026c-4804-b543-5396a8371b50','Majestic Star Dragon','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/7841112.jpg','Card'),
('2024-10-21',5.5,0,0,'b492e317-0a1e-422a-97f8-5e8d6daeffce','Right Arm of the Forbidden One','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/70903634.jpg','Card'),
('2024-10-21',0.6,0,0,'18149c79-5cf0-43a5-bc23-62a0c2dbbdf7','Vulpix','Pokémon','EN','https://images.pokemontcg.io/sv6/26_hires.png','Card'),
('2024-10-21',1.43,0,0,'e1a80cd5-4337-4fdc-ae2d-699261976dd0','Gernia','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/77936940.jpg','Card'),
('2024-10-21',1,0,0,'224a4618-0207-45e9-88f8-6f9650bdb2cd','Rhystic Study','Magic','EN','https://cards.scryfall.io/normal/front/d/6/d6914dba-0d27-4055-ac34-b3ebf5802221.jpg?1600698439','Card'),
('2024-10-21',3.78,0,0,'f2e3a20d-4336-4c72-bf16-6f992375d37d','Lavalval Ignis','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/3989465.jpg','Card'),
('2024-10-21',1.9,0,0,'16a811da-2bf3-4862-9c29-7810e84a653e','Blue-Eyes White Dragon','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/89631141.jpg','Card'),
('2024-10-21',0.56,0,0,'7dd25234-c615-4390-8de9-7a3d2a7bdbbb','Black Skull Dragon','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/11901678.jpg','Card'),
('2024-10-21',0.2,0,0,'e747b266-df68-4419-90e0-8214ade03f7d','Reflect Damage','Magic','EN','https://cards.scryfall.io/normal/front/3/a/3a2bf39b-9665-426b-b618-eb731d24a1ee.jpg?1562718783','Card'),
('2024-10-21',3.47,0,0,'ba4afcd8-1029-497d-84a2-82cf753cfa1f','Left Arm of the Forbidden One','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/7902349.jpg','Card'),
('2024-10-21',15.52,0,0,'358a43cf-cd65-4710-adee-902d3af7c12b','Slifer the Sky Dragon','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/10000020.jpg','Card'),
('2024-10-21',0.01,0,0,'a6f4a8d2-59fa-4f46-bf2d-935ac0d44ee3','Subterranean Spirit','Magic','EN','https://cards.scryfall.io/normal/front/1/3/132e8aac-9698-45fa-8d64-b460fd5deffc.jpg?1562717853','Card'),
('2024-10-21',1.21,0,0,'1045c060-135f-49ca-bd03-9425b3676c98','Stardust Dragon','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/44508094.jpg','Card'),
('2024-10-21',5.4,0,0,'359d4457-8eb9-4f8e-b26c-9cabdadd2d0c','Skull Red Bird','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/10202894.jpg','Card'),
('2024-10-21',5,0,0,'b23c6a08-457d-45be-a97d-9d84578d7f54','Arcanine ex','Pokémon','EN','https://images.pokemontcg.io/sv1/32_hires.png','Card'),
('2024-10-21',1,0,0,'3ce5c8b4-0746-49b8-9029-9fa72b04d642','Timetwister','Magic','EN','https://cards.scryfall.io/normal/front/f/b/fbee1e10-0b8c-44ea-b0e5-44cdd0bfcd76.jpg?1614638835','Card'),
('2024-10-21',0.2,0,0,'f0b2f429-4447-40b0-9caf-a13d2ed41076','Killer Needle','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/88979991.jpg','Card'),
('2024-10-21',3.9,0,0,'109f12ad-071f-4531-9380-a2295f397778','Gholdengo','Pokémon','EN','https://images.pokemontcg.io/sv4pt5/67_hires.png','Card'),
('2024-10-21',1,0,0,'dd9d5e3b-9b6c-4245-8b3a-a9684a7d2318','Curse of Marit Lage','Magic','EN','https://cards.scryfall.io/normal/front/6/9/69b381c1-aa71-4d40-a320-70f58a440d51.jpg?1587911900','Card'),
('2024-10-21',9,0,0,'571d7af9-d20d-4325-a21d-aab5c7c1c8ad','Left Leg of the Forbidden One','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/44519536.jpg','Card'),
('2024-10-21',1.78,0,0,'d728f7f2-084e-4c88-8635-b0e2b7c70d71','Perception Bobblehead','Magic','EN','https://cards.scryfall.io/normal/front/e/2/e2a8c89c-2cd1-4f68-9758-449177be2880.jpg?1708742759','Card'),
('2024-10-21',5.89,0,0,'bea0d9e3-c9a0-43ee-8f93-bef2c0e0592a','Likeness Looter','Magic','EN','https://cards.scryfall.io/normal/front/2/9/2957472a-825e-4904-b7e8-62bef1cb432d.jpg?1692939367','Card'),
('2024-10-21',7.9,0,0,'2b6e084e-6ded-4721-b21b-bfc861b203ef','Black Luster Soldier','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/5405694.jpg','Card'),
('2024-10-21',0.5,0,0,'312443f6-3f9b-490c-9a83-d68ee889365d','Feraligatr','Pokémon','EN','https://images.pokemontcg.io/swsh8/57_hires.png','Card'),
('2024-10-21',52.5,0,0,'3c70cb6b-44bf-4c7e-8fed-d9405d41f156','Mew ex','Pokémon','EN','https://images.pokemontcg.io/sv3pt5/205_hires.png','Card'),
('2024-10-21',5,0,0,'fa59eef9-75e2-45cf-a6e6-ea88fa3683a1','Mimikyu','Pokémon','EN','https://images.pokemontcg.io/svp/75_hires.png','Card'),
('2024-10-21',6.7,0,0,'2d130ebf-0714-4f21-8b7c-eb65dcc9a164','Magician\'s Rod','Yu-Gi-Oh!','EN','https://images.ygoprodeck.com/images/cards/7084129.jpg','Card'),
('2024-10-21',1,0,0,'1e74d49e-6bbb-4924-8e00-f19285d95f8a','Seasons Past','Magic','EN','https://cards.scryfall.io/normal/front/6/6/668afd78-3cf5-4daf-8dfb-fca90de0ae5a.jpg?1576385218','Card'),
('2024-10-21',3.9,0,0,'54762caf-9257-43b2-8825-f48469d6d2ac','Magcargo','Pokémon','EN','https://images.pokemontcg.io/sv5/29_hires.png','Card'),
('2024-10-21',8.4,0,0,'4ae7a060-c2e6-4401-92e1-f7e8203d2976','Volcanic Island','Magic','EN','https://cards.scryfall.io/normal/front/2/f/2f607e7e-30c0-45e9-8f61-bf6e9fe63f2b.jpg?1562904669','Card');
/*!40000 ALTER TABLE `product_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `role` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `role`
--

LOCK TABLES `role` WRITE;
/*!40000 ALTER TABLE `role` DISABLE KEYS */;
INSERT INTO `role` VALUES
(1,'ROLE_SELLER'),
(2,'ROLE_ADMIN'),
(3,'ROLE_BUYER');
/*!40000 ALTER TABLE `role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transactions` (
  `amount` double DEFAULT NULL,
  `currency` tinyint(4) DEFAULT NULL CHECK (`currency` between 0 and 0),
  `type` bit(1) NOT NULL,
  `date` datetime(6) NOT NULL,
  `id` uuid NOT NULL,
  `orders_id` uuid DEFAULT NULL,
  `product_id` uuid DEFAULT NULL,
  `wallet_id` uuid NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKb08tgp3a21g4bm22duyugqnhe` (`orders_id`),
  KEY `FKf3rvblhfww1l62p4f65y0guox` (`product_id`),
  KEY `FK1y5uaof4j1b7fo1ldgkunxq9t` (`wallet_id`),
  CONSTRAINT `FK1y5uaof4j1b7fo1ldgkunxq9t` FOREIGN KEY (`wallet_id`) REFERENCES `wallet` (`id`),
  CONSTRAINT `FKb08tgp3a21g4bm22duyugqnhe` FOREIGN KEY (`orders_id`) REFERENCES `orders` (`id`),
  CONSTRAINT `FKf3rvblhfww1l62p4f65y0guox` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `blocked` bit(1) NOT NULL,
  `id` uuid NOT NULL,
  `cellphone_number` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ob8kqyqqgmefl0aco34akdtpe` (`email`),
  UNIQUE KEY `UK_sb8bbouer5wak8vyiiy4pf2bx` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES
('\0','a93d9db9-3718-461e-b650-2a51d96f0099','1234567891','mariorossi12@gmail.com','$2a$10$GZ8kJf5kP8k5LngAtU6/A.kgP3QqHPy3sqPrMpwk7fdZqEzo2MMHu','Mario'),
('\0','08ee6756-8b72-4854-82f2-3336d8b0de57','1287549234','yugi00@gmail.com','$2a$10$6ljZ8lEb7HAz1BOMWRshF.sa9k1kQCmJ56a4qABwh.7c58DlNyT6K','Yugi'),
('\0','8f45c34f-769d-4329-8282-411f6428bae2','4445556666','alessiosturniolo2901@gmail.com','$2a$10$IKJEUZvE8A4yeLGeHw2rq.mUENnPREfC9W93YNseoS0u3m/ftPaGm','Alessio'),
('\0','7ea5051d-c1c5-4445-9b4d-439fef412cf8','0987654321','francescomorrone18@gmail.com','$2a$10$KIrxDBzAqgUWNTAFcdGsxOEybtOQOAyway/cmqw7MNlWqC8t267Ky','Francesco'),
('\0','83977505-df9f-4716-9c7c-461f4a5d3395','1234567890','magiclife007@gmail.com','$2a$10$UGet/XKTVa3jvWsnTTPejezxb9N86WiJRqvmOVg6u0Ynp/3pgL2jK','MagicLife'),
('\0','992674df-badc-41d8-8829-4a7d61dff9e6','1234567897','magicking78@gmail.com','$2a$10$YPurvoEXQDHKlMzOVJGtTuOReWRCS20SMYaP7S7yjHsxjZfuB9yg2','MagicKing'),
('\0','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4','1234567891','luigiverdi01@gmail.com','$2a$10$y4WcYVE2x5YTiaGBrHYFPeEFM94mlzFC233C5Xpul.x3JdzmVr3Im','KingOfCard'),
('\0','a6912692-a2ab-4656-a345-b30d285bec0f','1234567890','matteocanino1802@gmail.com','$2a$10$7FItm/UKthOLgdTu0JVFKOJSH4PeACTWy9zgpg.85nm8ufQaIR.yW','Matteo'),
('\0','a2f74c99-1b5d-459a-a6c6-dc9abc7feedc','1112223333','pier.napoli@yahoo.it','$2a$10$/AUhfxK8.QciM1rJpR92o.kpyI5fgpfrlSF/Tm34UoRQ/wOB7/Lnm','Pierfrancesco'),
('\0','9ffea693-6d2c-4a2c-8e04-eb9055668061','7639862756','dragonmaster90@gmail.com','$2a$10$lUI4grWrqlRBpwNuvVzCWOd28LSVX4QyJUs8vp1JRSTzYE/YXK15m','DragonMaster');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_role`
--

DROP TABLE IF EXISTS `user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_role` (
  `role_id` bigint(20) NOT NULL,
  `user_id` uuid NOT NULL,
  PRIMARY KEY (`role_id`,`user_id`),
  KEY `FK859n2jvi8ivhui0rl0esws6o` (`user_id`),
  CONSTRAINT `FK859n2jvi8ivhui0rl0esws6o` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKa68196081fvovjhkek5m97n3y` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_role`
--

LOCK TABLES `user_role` WRITE;
/*!40000 ALTER TABLE `user_role` DISABLE KEYS */;
INSERT INTO `user_role` VALUES
(1,'a93d9db9-3718-461e-b650-2a51d96f0099'),
(3,'a93d9db9-3718-461e-b650-2a51d96f0099'),
(1,'08ee6756-8b72-4854-82f2-3336d8b0de57'),
(3,'08ee6756-8b72-4854-82f2-3336d8b0de57'),
(2,'8f45c34f-769d-4329-8282-411f6428bae2'),
(2,'7ea5051d-c1c5-4445-9b4d-439fef412cf8'),
(1,'83977505-df9f-4716-9c7c-461f4a5d3395'),
(3,'83977505-df9f-4716-9c7c-461f4a5d3395'),
(1,'992674df-badc-41d8-8829-4a7d61dff9e6'),
(3,'992674df-badc-41d8-8829-4a7d61dff9e6'),
(1,'f7616c8b-22ca-4f51-8a1a-5b0de95c55e4'),
(3,'f7616c8b-22ca-4f51-8a1a-5b0de95c55e4'),
(2,'a6912692-a2ab-4656-a345-b30d285bec0f'),
(2,'a2f74c99-1b5d-459a-a6c6-dc9abc7feedc'),
(1,'9ffea693-6d2c-4a2c-8e04-eb9055668061'),
(3,'9ffea693-6d2c-4a2c-8e04-eb9055668061');
/*!40000 ALTER TABLE `user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wallet`
--

DROP TABLE IF EXISTS `wallet`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wallet` (
  `amount` double DEFAULT NULL,
  `currency` tinyint(4) DEFAULT NULL CHECK (`currency` between 0 and 0),
  `id` uuid NOT NULL,
  `user_id` uuid NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_hgee4p1hiwadqinr0avxlq4eb` (`user_id`),
  CONSTRAINT `FKbs4ogwiknsup4rpw8d47qw9dx` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wallet`
--

LOCK TABLES `wallet` WRITE;
/*!40000 ALTER TABLE `wallet` DISABLE KEYS */;
INSERT INTO `wallet` VALUES
(0,0,'42915199-1fd7-4975-95ac-030155882ac7','a93d9db9-3718-461e-b650-2a51d96f0099'),
(0,0,'f53ff3cf-e837-4e79-92a6-236652da31d7','992674df-badc-41d8-8829-4a7d61dff9e6'),
(0,0,'bb28a39f-ec39-4fa5-bad1-3246257a17b7','9ffea693-6d2c-4a2c-8e04-eb9055668061'),
(0,0,'4fb20970-98bb-44b0-9ca8-4a19c32abd24','f7616c8b-22ca-4f51-8a1a-5b0de95c55e4'),
(0,0,'cd137dab-73ee-4718-a0a4-95f2427e3466','08ee6756-8b72-4854-82f2-3336d8b0de57'),
(0,0,'a47dc11c-def8-42b7-9402-c6f5c9e0c20d','83977505-df9f-4716-9c7c-461f4a5d3395');
/*!40000 ALTER TABLE `wallet` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlist`
--

DROP TABLE IF EXISTS `wishlist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wishlist` (
  `last_update` datetime(6) NOT NULL,
  `id` uuid NOT NULL,
  `wishlist_id` uuid DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_5m23j2ywetqqsxr4cu8ifq0xr` (`wishlist_id`),
  CONSTRAINT `FKbf8tmosah21qrwq12gdm1kery` FOREIGN KEY (`wishlist_id`) REFERENCES `capability_token` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlist`
--

LOCK TABLES `wishlist` WRITE;
/*!40000 ALTER TABLE `wishlist` DISABLE KEYS */;
/*!40000 ALTER TABLE `wishlist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wishlist_product`
--

DROP TABLE IF EXISTS `wishlist_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `wishlist_product` (
  `product_id` uuid NOT NULL,
  `wishlist_id` uuid NOT NULL,
  PRIMARY KEY (`product_id`,`wishlist_id`),
  KEY `FK6qi207s5p27bm3qmkxpk1fv8o` (`wishlist_id`),
  CONSTRAINT `FK6qi207s5p27bm3qmkxpk1fv8o` FOREIGN KEY (`wishlist_id`) REFERENCES `wishlist` (`id`),
  CONSTRAINT `FKsqs4r107po6y96en1si6pryx7` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wishlist_product`
--

LOCK TABLES `wishlist_product` WRITE;
/*!40000 ALTER TABLE `wishlist_product` DISABLE KEYS */;
/*!40000 ALTER TABLE `wishlist_product` ENABLE KEYS */;
UNLOCK TABLES;

/* Trigger per eliminare i token scaduti */
DELIMITER //

CREATE EVENT update_invalidated_token
ON SCHEDULE EVERY 1 DAY
STARTS '2024-10-12 10:02:02.000'
ON COMPLETION NOT PRESERVE
ENABLE
DO BEGIN
    DELETE FROM OnlyCards.invalidated_token 
    WHERE expiry_date <= NOW();
END //

DELIMITER ;

/* Evento per settare lo stato di un ordine spedito come consegnato dopo 5 giorni */
DELIMITER //

CREATE EVENT update_orders_status
ON SCHEDULE EVERY 5 MINUTE 
DO
BEGIN
    UPDATE orders
    SET status = 2
    WHERE status = 1 
      AND modify_date <= NOW() - INTERVAL 5 DAY;
END //

DELIMITER ;

--
-- Dumping routines for database 'OnlyCards'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2024-09-30 19:29:54