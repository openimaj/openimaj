# OpenIMAJ Build Notes

## Compiling and installing locally:

With tests:

    mvn install

Tests disabled:

    mvn install -DskipTests

## Building the OI site:

Staging locally:

    mvn site site:stage -Darguments=-DskipTests

Deploying to GH-Pages:

    mvn site site:deploy -Darguments=-DskipTests

## Adding/checking license info
	
	mvn license:format
	mvn license:check

## Making a release:

Note: remember to do all steps to ensure that both maven.openimaj.org and maven central are updated

	mvn release:prepare
	mvn release:perform -Dgoals="deploy site-deploy"	
	mvn deploy -DskipTests -Darguments=-DskipTests -P central-deployment
	mvn nexus-staging:release

