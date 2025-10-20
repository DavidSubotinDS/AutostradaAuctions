USE AutostradaAuctions;

-- Clear existing data first
DELETE FROM Bids;
DELETE FROM Auctions;
DELETE FROM Vehicles;

-- Insert Vehicles
INSERT INTO Vehicles (Make, Model, Year, Mileage, Color, VIN, FuelType, Transmission, Description, ImageUrls, CreatedAt) VALUES
('Porsche', '911 Turbo S', 2023, 2500, 'Guards Red', 'WP0AB2A99NS012345', 'Gasoline', 'Automatic', 'Brand new Porsche 911 Turbo S with only 2,500 miles. This is the ultimate sports car with 640hp and all-wheel drive. Features include ceramic brakes, sport chrono package, and premium leather interior.', 'https://images.unsplash.com/photo-1611821064430-077c8b3ca5ac?w=800', GETDATE()),
('Lamborghini', 'Huracán STO', 2022, 1200, 'Arancio Borealis', 'ZHWUC2ZF9NLA12345', 'Gasoline', 'Automatic', 'Track-focused Lamborghini Huracán STO with just 1,200 miles. This limited edition supercar features advanced aerodynamics, lightweight construction, and 631hp of naturally aspirated V10 power.', 'https://images.unsplash.com/photo-1617814076367-b759c7d7e738?w=800', GETDATE()),
('Ferrari', '488 GTB', 2021, 3800, 'Rosso Corsa', 'ZFF79ALA4M0123456', 'Gasoline', 'Automatic', 'Stunning Ferrari 488 GTB in classic Rosso Corsa red. This mid-engine masterpiece produces 661hp and features carbon fiber trim, premium alcantara interior, and the legendary Ferrari driving experience.', 'https://images.unsplash.com/photo-1592198084033-aade902d1aae?w=800', GETDATE()),
('McLaren', '720S', 2020, 5200, 'Azores Blue', 'SBM14DCA6LW012345', 'Gasoline', 'Automatic', 'McLaren 720S in stunning Azures Blue with 5,200 miles. Features the revolutionary MonoCell II carbon fiber chassis, active aerodynamics, and 710hp twin-turbo V8 engine.', 'https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=800', GETDATE()),
('Aston Martin', 'DBS Superleggera', 2022, 800, 'British Racing Green', 'SCFRMFAW2NGJ12345', 'Gasoline', 'Automatic', 'Practically new Aston Martin DBS Superleggera with only 800 miles. This grand tourer combines luxury and performance with 715hp V12 engine, carbon fiber bodywork, and exquisite craftsmanship.', 'https://images.unsplash.com/photo-1571607388263-1044f9ea01dd?w=800', GETDATE()),
('BMW', 'M4 Competition', 2023, 1500, 'Alpine White', 'WBS8M9C09P5A12345', 'Gasoline', 'Automatic', 'Brand new BMW M4 Competition with just 1,500 miles. Features the S58 twin-turbo inline-6 producing 503hp, M xDrive all-wheel drive, carbon fiber roof, and competition package.', 'https://images.unsplash.com/photo-1555215695-3004980ad54e?w=800', GETDATE()),
('Mercedes-AMG', 'GT Black Series', 2022, 900, 'Magno Black', 'WDDYJ7JA8NA123456', 'Gasoline', 'Automatic', 'Ultra-rare Mercedes-AMG GT Black Series with only 900 miles. The most powerful AMG GT ever made with 720hp, active aerodynamics, and track-focused engineering. Limited production.', 'https://images.unsplash.com/photo-1563720223185-11003d516935?w=800', GETDATE()),
('Audi', 'R8 V10 Plus', 2021, 2100, 'Nardo Gray', 'WUAANAFY6M7012345', 'Gasoline', 'Automatic', 'Audi R8 V10 Plus in sophisticated Nardo Gray with 2,100 miles. Features the naturally aspirated 5.2L V10 producing 602hp, quattro all-wheel drive, and carbon fiber elements throughout.', 'https://images.unsplash.com/photo-1606664515524-ed2f786a0bd6?w=800', GETDATE());

-- Insert Live Auctions (Status 1 = Active)
INSERT INTO Auctions (VehicleId, SellerId, Title, Description, StartingPrice, CurrentBid, StartTime, EndTime, Status, HasReserve, SubmittedAt, CreatedAt) VALUES
(1, 9, '2023 Porsche 911 Turbo S - Guards Red Beauty', 'Rare opportunity to own a practically new 2023 Porsche 911 Turbo S in the iconic Guards Red color. With only 2,500 miles, this supercar delivers 640hp and 0-60 in just 2.6 seconds. Full warranty remaining.', 150000.00, 165000.00, DATEADD(HOUR, -2, GETDATE()), DATEADD(DAY, 3, GETDATE()), 1, 0, DATEADD(HOUR, -2, GETDATE()), GETDATE()),
(2, 10, '2022 Lamborghini Huracán STO - Track Monster', 'Ultra-exclusive Lamborghini Huracán STO in Arancio Borealis with just 1,200 miles. This track-focused beast features advanced aerodynamics and 631hp of naturally aspirated V10 fury. Certificate of authenticity included.', 280000.00, 295000.00, DATEADD(HOUR, -1, GETDATE()), DATEADD(DAY, 2, GETDATE()), 1, 0, DATEADD(HOUR, -1, GETDATE()), GETDATE()),
(3, 11, '2021 Ferrari 488 GTB - Rosso Corsa Perfection', 'Stunning Ferrari 488 GTB in the classic Rosso Corsa red that defines Ferrari. 3,800 miles of pure Italian passion with 661hp twin-turbo V8. Meticulously maintained with full service history.', 220000.00, 235000.00, DATEADD(MINUTE, -30, GETDATE()), DATEADD(DAY, 4, GETDATE()), 1, 0, DATEADD(MINUTE, -30, GETDATE()), GETDATE()),
(4, 9, '2020 McLaren 720S - Azures Blue Masterpiece', 'McLaren 720S in the stunning Azures Blue with 5,200 miles. Revolutionary MonoCell II chassis and 710hp make this the ultimate supercar experience. Active aerodynamics and luxury appointments throughout.', 200000.00, 215000.00, DATEADD(HOUR, -3, GETDATE()), DATEADD(DAY, 5, GETDATE()), 1, 0, DATEADD(HOUR, -3, GETDATE()), GETDATE()),
(5, 10, '2022 Aston Martin DBS Superleggera - British Elegance', 'Practically new Aston Martin DBS Superleggera in British Racing Green with only 800 miles. 715hp V12 grand tourer that combines luxury and performance. Hand-crafted interior with premium materials.', 250000.00, 260000.00, DATEADD(MINUTE, -45, GETDATE()), DATEADD(DAY, 1, GETDATE()), 1, 0, DATEADD(MINUTE, -45, GETDATE()), GETDATE()),
(6, 11, '2023 BMW M4 Competition - Alpine White Excellence', 'Brand new BMW M4 Competition with just 1,500 miles. S58 twin-turbo engine with 503hp and M xDrive. Competition package includes carbon fiber details and track-ready suspension.', 85000.00, 92000.00, DATEADD(HOUR, -4, GETDATE()), DATEADD(DAY, 6, GETDATE()), 1, 0, DATEADD(HOUR, -4, GETDATE()), GETDATE()),
(7, 9, '2022 Mercedes-AMG GT Black Series - Ultimate AMG', 'Ultra-rare Mercedes-AMG GT Black Series with only 900 miles. The most powerful AMG GT ever with 720hp and track-focused engineering. Limited production makes this a future classic.', 350000.00, 365000.00, DATEADD(MINUTE, -15, GETDATE()), DATEADD(HOUR, 18, GETDATE()), 1, 0, DATEADD(MINUTE, -15, GETDATE()), GETDATE()),
(8, 10, '2021 Audi R8 V10 Plus - Nardo Gray Sophistication', 'Audi R8 V10 Plus in sophisticated Nardo Gray with 2,100 miles. Naturally aspirated 5.2L V10 with 602hp and quattro all-wheel drive. Carbon fiber accents and premium interior appointments.', 160000.00, 172000.00, DATEADD(HOUR, -1, GETDATE()), DATEADD(DAY, 7, GETDATE()), 1, 0, DATEADD(HOUR, -1, GETDATE()), GETDATE());

-- Add some realistic bid history
INSERT INTO Bids (AuctionId, BidderId, Amount, Timestamp, IsWinning) VALUES
-- Porsche 911 Turbo S bids
(1, 8, 150000.00, DATEADD(HOUR, -2, GETDATE()), 0),
(1, 9, 155000.00, DATEADD(MINUTE, -110, GETDATE()), 0),
(1, 8, 160000.00, DATEADD(MINUTE, -95, GETDATE()), 0),
(1, 11, 165000.00, DATEADD(MINUTE, -80, GETDATE()), 1),

-- Lamborghini Huracán STO bids
(2, 7, 280000.00, DATEADD(HOUR, -1, GETDATE()), 0),
(2, 8, 285000.00, DATEADD(MINUTE, -50, GETDATE()), 0),
(2, 11, 290000.00, DATEADD(MINUTE, -35, GETDATE()), 0),
(2, 7, 295000.00, DATEADD(MINUTE, -20, GETDATE()), 1),

-- Ferrari 488 GTB bids
(3, 9, 220000.00, DATEADD(MINUTE, -30, GETDATE()), 0),
(3, 8, 225000.00, DATEADD(MINUTE, -25, GETDATE()), 0),
(3, 10, 230000.00, DATEADD(MINUTE, -15, GETDATE()), 0),
(3, 8, 235000.00, DATEADD(MINUTE, -10, GETDATE()), 1),

-- McLaren 720S bids
(4, 7, 200000.00, DATEADD(HOUR, -3, GETDATE()), 0),
(4, 11, 205000.00, DATEADD(MINUTE, -160, GETDATE()), 0),
(4, 8, 210000.00, DATEADD(MINUTE, -140, GETDATE()), 0),
(4, 10, 215000.00, DATEADD(MINUTE, -120, GETDATE()), 1),

-- Aston Martin DBS bids
(5, 8, 250000.00, DATEADD(MINUTE, -45, GETDATE()), 0),
(5, 7, 255000.00, DATEADD(MINUTE, -35, GETDATE()), 0),
(5, 11, 260000.00, DATEADD(MINUTE, -25, GETDATE()), 1),

-- BMW M4 Competition bids
(6, 7, 85000.00, DATEADD(HOUR, -4, GETDATE()), 0),
(6, 8, 88000.00, DATEADD(MINUTE, -220, GETDATE()), 0),
(6, 10, 90000.00, DATEADD(MINUTE, -200, GETDATE()), 0),
(6, 8, 92000.00, DATEADD(MINUTE, -180, GETDATE()), 1),

-- Mercedes-AMG GT Black Series bids
(7, 11, 350000.00, DATEADD(MINUTE, -15, GETDATE()), 0),
(7, 8, 355000.00, DATEADD(MINUTE, -12, GETDATE()), 0),
(7, 10, 360000.00, DATEADD(MINUTE, -8, GETDATE()), 0),
(7, 8, 365000.00, DATEADD(MINUTE, -5, GETDATE()), 1),

-- Audi R8 V10 Plus bids
(8, 9, 160000.00, DATEADD(HOUR, -1, GETDATE()), 0),
(8, 7, 165000.00, DATEADD(MINUTE, -50, GETDATE()), 0),
(8, 11, 170000.00, DATEADD(MINUTE, -40, GETDATE()), 0),
(8, 7, 172000.00, DATEADD(MINUTE, -30, GETDATE()), 1);

SELECT 'Data insertion completed successfully!' as Result;