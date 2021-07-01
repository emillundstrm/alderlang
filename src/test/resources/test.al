
import stdlib
import io


# Since there is no native toString a -> String
let show int =
    case int of
    | 0 -> "0"
    | 1 -> "1"
    | 2 -> "2"
    | 3 -> "3"
    | 4 -> "4"
    | 5 -> "5"
    | 6 -> "6"
    | 7 -> "7"
    | 8 -> "8"
    | 9 -> "9"
    | x -> concat (chars (show (x / 10))) (chars (show (x % 10)))
        |> string

let isEven x = eq 0 (x % 2)

let first (Pair a b) = b

let main = partition isEven (range 0 20) |> first |> map (compose println show) |> listIO

# 0 0 3 6 10 15 21 28 36 45 55 66
# (n-1)n/2

let main = quickSort (x -> y -> x - y) (range 1 50) |> map (compose println show) |> listIO
