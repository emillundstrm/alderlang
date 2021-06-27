

let seqIO a b = chainIO a (x -> b)

let listIO l = case l of
    | (Cons x xs) -> chainIO x (z -> listIO xs)
    | Nil -> noop

