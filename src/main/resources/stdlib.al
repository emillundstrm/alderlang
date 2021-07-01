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

let if cond ifTrue ifFalse =
    case cond of
        | True  -> ifTrue
        | False -> ifFalse

let and b1 b2 = if b1 b2 False

let or b1 b1 = if b1 True b2

let not b = case b of True -> False | False -> True

let complement f x = not (f x)