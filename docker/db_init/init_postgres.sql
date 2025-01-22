CREATE DATABASE eduvirt;

CREATE USER eduvirtadmin password 'eduvirtadmin';
CREATE USER eduvirt password 'eduvirt';

GRANT ALL ON DATABASE eduvirt TO eduvirtadmin;
ALTER DATABASE eduvirt OWNER TO eduvirtadmin;