# Core Issuer Switch — Visa Simulator (Raw Java)

A from-scratch ISO 8583 issuer switch in pure Java 17+. No frameworks, no
external libraries. A multi-threaded TCP server speaks a length-prefixed
ISO 8583 dialect, parses the bitmap and data elements by hand, and authorizes
transactions against an in-memory account store.

## Wire format

```
[ 4 ASCII digits = body length N ][ N bytes of body ]
body = [ MTI : 4 ][ Bitmap : 16 or 32 ASCII hex ][ DE2 ][ DE3 ] ...
```

* Fixed fields: exactly their catalog width.
* LLVAR fields: 2 ASCII length digits + value.
* LLLVAR fields: 3 ASCII length digits + value.

## Supported transactions

| MTI  | Proc code | Meaning          | Response MTI |
|------|-----------|------------------|--------------|
| 0200 | 000000    | Sale / Purchase  | 0210         |
| 0100 | 310000    | Balance Inquiry  | 0110         |
| 0400 | (any)     | Reversal         | 0410         |

Field 39 response codes: `00` approved, `51` insufficient funds,
`14` invalid card, `12` invalid transaction, `25` unable to locate original,
`96` system malfunction.

## Seeded test cards

| PAN              | Balance   |
|------------------|-----------|
| 4111111111111111 | $1,000.00 |
| 4222222222222222 | $50.00    |
| 4000000000000002 | $0.00     |

## Build & run (JDK 17+)

```sh
javac -d out $(find src -name '*.java')
java  -cp out com.issuer.switchsim.Main          # listens on 8583
java  -cp out com.issuer.switchsim.client.TestClient   # drives a demo
```
