drop table if exists colour;
CREATE TABLE colour
(
   flickrid INT IDENTITY(1,1) PRIMARY KEY,
   r DECIMAL,
   g DECIMAL,
   b DECIMAL
);
CREATE TABLE latlong
(
   flickrid INT IDENTITY(1,1) PRIMARY KEY,
   lat DECIMAL,
   lon DECIMAL
);