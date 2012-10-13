package com.verknowsys.served.web


import com.verknowsys.served._
import com.verknowsys.served.api._
import java.lang._


object merch {

    object implicits {
        implicit def anyElement2String(s: BaseElement) = s.toString
    }

    import com.verknowsys.served.db._


    abstract class BaseElement


    abstract class Size extends BaseElement
    object Size {
        case object XS extends Size
        case object S extends Size
        case object M extends Size
        case object L extends Size
        case object XL extends Size
        case object XXL extends Size

        def apply(a: String) = a match {
            case "XS" => XS
            case "S" => S
            case "M" => M
            case "L" => L
            case "XL" => XL
            case "XXL" => XXL
        }

        def values = XS :: S :: M :: L :: XL :: XXL :: Nil
    }


    abstract class Color extends BaseElement
    object Color {
        case object Black extends Color
        case object Red extends Color
        case object Blue extends Color
        case object Yellow extends Color
        case object White extends Color

        def apply(a: String) = a match {
            case "Black" => Black
            case "Red" => Red
            case "Blue" => Blue
            case "Yellow" => Yellow
            case "White" => White
        }

        def values = Black :: Red :: Blue :: Yellow :: White :: Nil
    }


    abstract class PrintSize extends BaseElement
    object PrintSize {
        case object A3 extends PrintSize
        case object A4 extends PrintSize
        case object A5 extends PrintSize
        case object A6 extends PrintSize

        def apply(a: String) = a match {
            case "A3" => A3
            case "A4" => A4
            case "A5" => A5
            case "A6" => A6
        }

        def values = A3 :: A4 :: A5 :: A6 :: Nil
    }


    abstract class ClothingCategory extends BaseElement
    object ClothingCategory {
        case object Tshirt extends ClothingCategory
        case object Blouse extends ClothingCategory
        case object Polo extends ClothingCategory

        def apply(a: String) = a match {
            case "Tshirt" => Tshirt
            case "Blouse" => Blouse
            case "Polo" => Polo
        }

        def values = Tshirt :: Blouse :: Polo :: Nil
    }


    abstract class TshirtPrintPlacement extends BaseElement
    object TshirtPrintPlacement {
        case object Front extends TshirtPrintPlacement
        case object Rear extends TshirtPrintPlacement
        case object Sleeve extends TshirtPrintPlacement

        def apply(a: String) = a match {
            case "Front" => Front
            case "Rear" => Rear
            case "Sleeve" => Sleeve
        }

        def values = Front :: Rear :: Sleeve :: Nil
    }


    abstract class ProjectType extends BaseElement
    object ProjectType {
        case object UserDefined extends ProjectType {
            def apply(fileAttached: String) = {
                // TODO
                // logger.warn("User File Attached: %s".format(fileAttached))
            }
        }
        case object PredefinedVector extends ProjectType
        case object PredefinedPrinted extends ProjectType

        def apply(a: String) = a match {
            case "PredefinedVector" => PredefinedVector
            case "PredefinedPrinted" => PredefinedPrinted
            case "UserDefined" => UserDefined
        }

        def values = PredefinedVector :: PredefinedPrinted :: UserDefined :: Nil
    }


    abstract class StickerType extends BaseElement
    object StickerType {
        case object Standard extends StickerType
        case object Car extends StickerType
        case object Wall extends StickerType
        case object Transparent extends StickerType

        def apply(a: String) = a match {
            case "Standard" => Standard
            case "Car" => Car
            case "Wall" => Wall
            case "Transparent" => Transparent
        }

        def values = Standard :: Car :: Wall :: Transparent :: Nil
    }



    // Model classes:

    case class Clothing(
            category: ClothingCategory = ClothingCategory.Tshirt,
            color: Color = Color.White,
            price: BigDecimal = 0,
            printSize: PrintSize = PrintSize.A4,
            printPlacement: TshirtPrintPlacement = TshirtPrintPlacement.Front,
            project: ProjectType = ProjectType.PredefinedVector,
            size: Size = Size.M,
            description: String = "No description",
            uuid: UUID = randomUUID
        ) extends Persistent


    case class SpatialLetter(
            color: Color = Color.White,
            price: BigDecimal = 0,
            project: ProjectType = ProjectType.PredefinedVector,
            description: String = "No description",
            uuid: UUID = randomUUID
        ) extends Persistent


    case class Sticker(
            color: Color = Color.White,
            price: BigDecimal = 0,
            project: ProjectType = ProjectType.PredefinedVector,
            stickerType: StickerType = StickerType.Standard,
            description: String = "No description",
            uuid: UUID = randomUUID
        ) extends Persistent


    object SpatialLetters extends DB[SpatialLetter]
    object Stickers extends DB[Sticker]
    object Clothings extends DB[Clothing]

}
