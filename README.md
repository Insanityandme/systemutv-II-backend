# MyHappyPlants

### Instructions for environment variables
1. In your operating system be it Windows, Linux or Mac, set the environment variable `TREFLE_API_KEY` to your Trefle API key.
2. Now you can use this key in your code by calling `System.getenv("TREFLE_API_KEY")` to get the value of the environment variable!

SQLite Installation Guide
Steg-för-steg:
Se till att du har klonat och pullat den senaste versionen av https://github.com/Insanityandme/systemutv-II-project
Ladda ner SQLite:
För Windows: https://youtu.be/XA3w8tQnYCA?si=uTCPwxvkKf7wh8xU
För Mac os: https://youtu.be/PC4phLbiZgk?si=GhjSS8eOmXZs1Wbn
Ladda ner de nödvändiga filerna, .jar-filer, för att projektet ska fungera som avsett. Du hittar de krävda filerna på Discord-servern under kanalen 'länkar’. Säkerställ att de finns på en säker plats på din dator för att undvika att du råkar radera dem i framtiden.
Databasfilen finns i sökvägen 'src\main\resources' och heter 'myHappyPlantsDB.db. Om den inte finns där kan ni ladda ner databasfilen på Discord-servern under kanalen ‘länkar’
Öppna projektet och lägg till jar-filerna i ‘Dependencies’:

Tryck på ‘Apply’ och sen ‘Ok’
Kör projektet genom Maven. Se till att först köra ‘'javafx:compile'’ och sedan köra 'javafx:run' för att starta programmet
Färdigt

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
