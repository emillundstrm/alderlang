# Note, this does not yet compile.
# Sketch of how macros could work?

# When calling a macro, the argument is always of type Expr:
type Expr =
    | App Expr Expr
    | Fun Expr Expr
    | TypedValue String (List Expr)
    | IntLiteral Int
    | StringLiteral String

macro toJs expr = case expr of
    | (App f a) -> join [toJs f, "(",  toJs a ")"]
    | (Fun p b) -> join ["(function(", toJs p ,"){", toJs b, "})"]
    | (TypedValue constructor args) -> join ["[", constructor, map toJs args, "]" ]
    | IntLiteral i -> i
    | StringLiteral s -> s