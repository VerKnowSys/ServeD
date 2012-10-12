package com.verknowsys.served.web


import com.verknowsys.served.web.merch._


object Dict {


    def translationsPL = Map(
        "Cart contains no products." -> "Brak produktów w koszyku.",
        "No matching products found." -> "Nie znaleziono pasujących produktów.",
        "WiatrSerwis Shop" -> "Sklep WiatrSerwis",
        "Please confirm your order in our shop." -> "Proszę potwierdzić zamówienie w naszym sklepie.",
        "Define new product" -> "Zdefiniuj nowy produkt",
        "Enter new product information" -> "Wprowadź informację o produkcie",
        "Product category" -> "Kategoria produktu",
        "Product color" -> "Kolor produktu",
        "Product description" -> "Opis produktu",
        "Create product" -> "Utwórz produkt",
        "Product description cannot be empty" -> "Opis produktu nie może być pusty",
        "Yellow" -> "Żółty",
        "Black" -> "Czarny",
        "Red" -> "Czerwony",
        "Blue" -> "Niebieski",
        "White" -> "Biały",
        "Sticker" -> "Naklejka",
        "SpatialLetter" -> "Litery Przestrzenne",
        "Product list" -> "Lista produktów",
        "Add new product" -> "Dodaj nowy produkt",
        "UserDefined" -> "Ustalany przez użytkownika",
        "Front" -> "Przód",
        "Rear" -> "Tył",
        "Sleeve" -> "Rękaw",
        "PredefinedVector" -> "Predefiniowany zgrzewany",
        "PredefinedPrinted" -> "Predefiniowany nadruk",
        "Clothing" -> "Odzież",
        "Product class" -> "Klasa produktu",
        "Blouse" -> "Bluza",
        "Standard" -> "Standardowa (zwykła)",
        "Car" -> "Samochodowa",
        "Wall" -> "Ścienna",
        "Transparent" -> "Transparentna",
        "Product sticker type" -> "Rodzaj folii",
        "Product size" -> "Rozmiar odzieży",
        "Product project" -> "Typ projektu",
        "Product print size" -> "Wielkość nadruku",
        "Product print placement" -> "Umiejscowienie nadruku"
    )


    def translationsEN = Map(
        "SpatialLetter" -> "Spatial Letters",
        "UserDefined" -> "User defined",
        "PredefinedVector" -> "Predefined sealed",
        "PredefinedPrinted" -> "Predefined print"
    )


    def apply(word: String, lang: String = "pl") = lang match {

        case "pl" =>
            (translationsPL get word).getOrElse(word)

        case "en" =>
            (translationsEN get word).getOrElse(word)

        case _ =>
            word

    }


}
