run:
	./gradlew quarkusDev

lint:
	./gradlew spotlessApply

check:
	./gradlew spotlessCheck build

test:
	./gradlew test
