import stdlib

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

let fizzbuzz i =
    let divisibleBy x y = (eq (y % x) 0) in
        if (divisibleBy 15 i) "FizzBuzz"
        (if (divisibleBy 3 i) "Fizz"
        (if (divisibleBy 5 i) "Buzz"
        (show i)))

let main =
    range 1 100
    |> map (compose println fizzbuzz)
    |> listIO
