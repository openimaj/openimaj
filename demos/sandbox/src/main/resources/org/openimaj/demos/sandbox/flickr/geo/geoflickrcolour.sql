drop database if exists geoflickrcolour;
create database geoflickrcolour;
use geoflickrcolour;
drop table if exists colour;
CREATE TABLE colour
(
   flickrid BIGINT PRIMARY KEY,
   r FLOAT,
   g FLOAT,
   b FLOAT
);
CREATE TABLE latlong
(
   flickrid BIGINT PRIMARY KEY,
   lat FLOAT,
   lon FLOAT,
   taken DATETIME,
   added DATETIME
);