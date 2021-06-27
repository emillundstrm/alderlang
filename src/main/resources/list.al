type List a = Cons a (List a) | Nil

let head (Cons a b) = a

let tail (Cons a b) = b

let last list = case list of
    | (Cons a Nil) -> a
    | (Cons a b) -> last b

let reverse = fold (acc -> item -> Cons item acc) []

#let concat list1 list2 = fold (acc -> item -> Cons item acc) list2 (reverse list1)
let concat list1 list2 = case list1 of
    | Nil -> list2
    | (Cons a b) -> Cons a (concat b list2)

let fold f acc list =
    case list of
        | (Cons a b) -> fold f (f acc a) b
        | Nil        -> acc

let map f list =
    case list of
        | Nil        -> Nil
        | (Cons a b) -> Cons (f a) (map f b)

let unfoldr gen i =
    case gen i of
        | (Just (Pair a b)) -> Cons a (unfoldr gen b)
        | None -> Nil

let reduce f list =
    case list of
        | (Cons head tail) -> fold f head tail
        | Nil -> Nil

let repeat x n = case n of
    | 0 -> Nil
    | m -> Cons x (repeat x (minus m 1))

let flatten = reduce concat

# Really terrible complexity - unclear why.
#let flatmap f l = flatten (map f l)

let flatmap f l = case l of
    | (Cons x xs) -> concat (f x) (flatmap f xs)
    | Nil -> []
