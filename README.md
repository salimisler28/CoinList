# Coin List App

## How to build

After running the command

> ./gradlew build

The apk file will be located in 

> /app/build/outputs/apk/debug/app-debug.apk

or just clone the project and open in Android Studio and click Run

## Project Structure

### Data Layer

Data layer handles the network and database operations.

### Domain Layer

Domain layer handles the logic in the app such as Init and Refresh.

### Presentation Layer

Presentation Layer contains UI (Jetpack Compose) and ViewModel. UI interacts with ViewModel by screen state. There is not any navigation library, that's why it just show Detail Screen when you click an item in List Screen 


## Unit Testing

The app has unit tests for InitUseCase, which test how the app retrieves data from the database or the network.