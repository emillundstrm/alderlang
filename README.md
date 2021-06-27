

[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]


<a href="https://github.com/emillundstrm/alderlang">
<img src="misc/logo.png" alt="Logo">
</a>

# Alder Programming Language

“There are two ways of constructing a software design: One way is to make it so simple that there are obviously no deficiencies, and the other way is to make it so complicated that there are no obvious deficiencies. The first method is far more difficult.”

― C. A. R. Hoare

:warning: This is a personal project, for my own learning and enjoyment. If you are interested in how to parse and interpret a programming language, then
 stay around and take a look. If you are looking for something _useful_, you are in the wrong place.

## About The Project

### Motivation

The strongest motivation is just that this is a learning experience for me. Partly to learn Kotlin and partly to understand how a non-strict pure functional
 language works at its core.

### The Name

The name "Alder" was chosen randomly, because it has a nice sound. It is furthermore a reference to other programming languages named after trees, such as Oak
 and Elm.

I use .al as the file extension for its source code files, which has a double meaning: it's an acronym for Alder Language and the Swedish name for alder.

## The Language

The syntax is strongly inspired by Haskell, but there are some deviations. For example, the grammar is completely context-free as there are no indentation
 rules. Instead, keywords have been introduced for some constructs that would otherwise be ambiguous.

### Basic syntax

```
# This is a comment

# Primitive values
"string"
25
5.0

# Definitions
let x = 10
let addOne = a + 1
let add a b = a + b

# Anonymous functions
x -> x + 1
x -> y -> x * y

# Function application
addOne x
add x y
add (f x) (y - 1)
(x -> y -> x) "foo" "bar"
```

### More on function application

Function application is left-associative and has very high precedence. This makes sense for people familiar with Haskell, but for those who are used to other
 programming languages it can be confusing how parentheses must be used. Consider this example:
 
```
# Given:
# toString :: Int -> String
# length :: String -> Int
# myString :: String

# Incorrect - tries to call println with four arguments
let main = println toString length myString

# Correct - parentheses used to indicate application order
let main = println (toString (length myString)) 
```

This can cause an unwieldy amount of parentheses, which can complicate reading. For this reason, Alder has a special operator for function application that
 has very _low_ precedence (shamelessly borrowed from F#, and similar to $ in Haskell)
 
```
# Correct - uses left pipe operator (TODO: should this be right associative?)
let main = println <| toString (length myString) 

# Correct - uses right pipe operator
let main = myString |> length |> toString |> println
```

### Types

:warning: While types exist, type checking has not yet been implemented. It is VERY easy to create runtime errors that are very hard to debug.

Alder supports Product and Sum types, as well as a few primitive types:

```
# Primitive types 
type IOAction
type Int
type String

# Types defined by the prelude:
type Bool = True | False
type Pair a b = Pair a b
type Maybe m = Just m | None
type Result s f = Success s | Failure f
type List a = Cons a (List a) | Nil
```

If you are familiar with Haskell types this should be very familiar. 

### Pattern matching

Function parameters can be patterns. This is true for the `let` function definitions and for anonymous functions:

```
# Let syntax
let first (Pair x y) = x

# Anonymous syntax
let second = (Pair x y) -> y 
```

Note that the parentheses are required in this case.

`Pair x y` in these samples denotes a constructor, in this case the constructor for the Pair data type. When this function is invoked, the variables x and y
 will be bound to the corresponding values in the pair value.

Alder does not support matching against multiple patterns in a function definition. This means that for Sum types, case expressions must be used:

```
let map f list = case list of
    | (Cons x xs) -> Cons (f x) (map f xs)
    | Nil -> Nil 
```

Here the `list` argument will be matched against two different pattern-matching functions and use the first one which matches.

### IO

IO is in alpha state.

The idea right now is to make it work like Haskell, with an IO type.

### Strictness

Lazy evaluation is not always optimal. Sometimes, it can make sense to force the early evaluation of an expression.

The `seq` function (or `!!` operator) can be used for this.

TODO: Example for when this is useful.

## Running

Right now only an interpreter exists.

Use one of the overloads of Interpreter::run to parse and interpret a program. 

## Roadmap

Unordered:

- Type checking
- Type classes (or an alternative)
- More native functions for manipulation of primitive values
- Tail recursion
- Macros (functions of type Expression -> Any)
- Type-safe records

## License

Distributed under the MIT License. See `LICENSE` for more information.

<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/emillundstrm/alderlang.svg?style=for-the-badge
[contributors-url]: https://github.com/emillundstrm/alderlang/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/emillundstrm/alderlang.svg?style=for-the-badge
[forks-url]: https://github.com/emillundstrm/alderlang/network/members
[stars-shield]: https://img.shields.io/github/stars/emillundstrm/alderlang.svg?style=for-the-badge
[stars-url]: https://github.com/emillundstrm/alderlang/stargazers
[issues-shield]: https://img.shields.io/github/issues/emillundstrm/alderlang.svg?style=for-the-badge
[issues-url]: https://github.com/emillundstrm/alderlang/issues
[license-shield]: https://img.shields.io/github/license/emillundstrm/alderlang.svg?style=for-the-badge
[license-url]: https://github.com/emillundstrm/alderlang/blob/main/LICENSE
