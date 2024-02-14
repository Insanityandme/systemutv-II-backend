# MyHappyPlants

### Instructions for environment variables
1. In your operating system be it Windows, Linux or Mac, set the environment variable `TREFLE_API_KEY` to your Trefle API key.
2. Now you can use this key in your code by calling `System.getenv("TREFLE_API_KEY")` to get the value of the environment variable!


### OLD DESCRIPTION
### Produktbeskrivning
My Happy Plants är en applikation tänkt att hjälpa en användare att ta hand om sina växter i hemmet samt ge användaren information om växterna. My Happy Plants använder sig av information hämtad från Trefle.io, som var ett öppet och gratis API som erbjöd information om en miljon växtarter och hybrider. Applikationen omfattar ett färgglatt grafiskt användargränssnitt utvecklat i JavaFX med bilder av illustrerade växter, och ger möjlighet för användaren att söka bland tiotusentals växter, döpa dem och lägga till dem i sitt personliga bibliotek.
Applikationen påminner även användaren när det är tid att vattna, enligt appens beräkning.

### Instruktioner för att köra programmet
1. Se till att alla maven dependencies har laddats in
2. Execute maven goal "mvn javafx:compile"
3. Execute maven goal "mvn javafx:run" för att starta klienten
4. Kör main-metoden i se/myhappyplants/server/StartServer.java för att starta servern


Bilden nedan visar hur man exekverar ett maven goal.

![bild](https://user-images.githubusercontent.com/77005138/114137664-cd6c0d80-990c-11eb-8350-bdc3172e48d7.png)
