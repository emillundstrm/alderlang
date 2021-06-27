import list
import io

type Bool = True | False

type Pair a b = Pair a b

type Maybe m = Just m | None

type Result s f = Success s | Failure f

let fix = f -> (x -> f (x x)) (x -> f (x x))

let bottom = bottom

let compose f g x = f (g x)

let flip f x y = f y x

let range from to =
    let gen b = if (eq b to) None (Just (Pair b (plus b 1))) in
    unfoldr gen from

let iterate f = unfoldr (x -> Just (Pair x (f x)))

let if cond ifTrue ifFalse =
    case cond of
        | True  -> ifTrue
        | False -> ifFalse

let and b1 b2 = if b1 b2 False

let or b1 b1 = if b1 True b2
