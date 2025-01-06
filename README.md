# 마켓 백엔드 API (외부 결제, 장바구니, 주문관리) 개인프로젝트

---

## 목차

- [프로젝트 개요](#프로젝트-개요)
- [기능](#기능)
- [사용 기술](#사용-기술)
- [Endpoints](#endpoints)
  - [Product API](#product-api)
    - [1. 상품 등록](#1-상품-등록)
    - [2. 특정 상품 조회](#2-특정-상품-조회)
    - [3. 모든 상품 목록 조회](#3-모든-상품-목록-조회)
    - [4. 구매 가능한 상품 목록 조회](#4-구매-가능한-상품-목록-조회)
    - [5. 상품 업데이트](#5-상품-업데이트)
    - [6. 상품 삭제](#6-상품-삭제)
  - [Cart API](#cart-api)
    - [1. 장바구니 항목 조회](#1-장바구니-항목-조회)
    - [2. 장바구니에 추가](#2-장바구니에-추가)
    - [3. 장바구니 비우기](#3-장바구니-비우기)
  - [Order API](#order-api)
    - [1. 주문 상세 조회](#1-주문-상세-조회)
  - [Payment API](#payment-api)
    - [1. 주문 생성 및 결제](#1-주문-생성-및-결제)
    - [2. 결제 상세 조회](#2-결제-상세-조회)
- [Error Handling](#error-handling)
- [Examples](#examples)
- [테이블 관계도](#ERD)

---

## 프로젝트 개요

**올라마켓 백엔드 API**는 장바구니 관리, 주문 처리, 결제 관리와 같은 기본적인 전자상거래 기능을 처리하기 위해 설계된 백엔드 서비스입니다.  
이 프로젝트는 개인 학습 프로젝트로, Java와 Spring Boot를 사용하여 RESTful API를 구축하였습니다.

### 목표

- **상품 조회:** 구매 가능한 상품 목록을 불러옵니다.
- **장바구니:** 상품을 장바구니에 추가하고 수량을 관리합니다.
- **주문 및 결제:** 주문을 제출하고 결제를 처리합니다.
- **주문 내역 조회:** 사용자의 완료된 주문 기록을 조회합니다.

---

## 기능

- **상품 조회**
  - 전체 상품 조회
  - 구매 가능 상품 목록 조회

- **장바구니 관리**
  - 상품 추가, 업데이트, 삭제
  - 상품 추가시 재고 수량에 따른 결과 처리

- **주문 처리**
  - 장바구니 내용을 기반으로 주문 상세 정보 계산
  - 관련 주문 상품 및 상태와 함께 주문 영속화
  - 결제 결과에 따른 주문 상태값 변경 및 상품 재고 차감

- **결제 관리**
  - 외부 결제 API와 통합하여 결제 처리
  - 결제 상태 관리 및 주문 상태 업데이트

- **예외 처리**
  - `@ControllerAdvice`를 사용한 글로벌 예외 처리
  - 다양한 오류 시나리오에 대한 커스텀 예외

- **테스트**
  - JUnit과 Mockito를 사용한 서비스 계층 유닛 테스트
  - 컨트롤러 엔드포인트에 대한 MockMVC 테스트

---

## 사용 기술

- **프로그래밍 언어:** Java 21
- **프레임워크:** Spring Boot 3.4.0
- **데이터베이스:** MariaDb 10.6.20
- **ORM:** Hibernate/JPA
- **빌드 도구:** Maven/Gradle
- **테스트:** JUnit 5, Mockito, AssertJ
- **기타 도구:**
  - 외부 API 작업을 위한 RestTemplate
  - 코드 감소를 위한 Lombok
  - 로깅을 위한 SLF4J

---

## Endpoints

### Product API

`/api/v1/product`

#### 1. 상품 등록

- **URL:** `/api/v1/product`
- **Method:** `POST`
- **Description:** 여러 개의 상품을 생성합니다.
- **Request Body:**

  ```json
  [
    {
      "name": "Product A",
      "price": 1000,
      "stock": 50
    },
    {
      "name": "Product B",
      "price": 2000,
      "stock": 30
    }
  ]
  ```

- **필드 설명:**
  - `name` (String, 필수): 상품명
  - `price` (Long, 필수): 상품 가격
  - `stock` (Integer, 필수): 재고 수량

- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "SUCCESS"
    }
    ```

#### 2. 특정 상품 조회

- **URL:** `/api/v1/product/{productId}`
- **Method:** `GET`
- **Description:** 특정 상품의 상세 정보를 조회합니다.
- **Path Parameters:**

  | Parameter   | Type   | Description      |
  |-------------|--------|------------------|
  | `productId` | `long` | 조회할 상품의 ID |

- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "SUCCESS",
      "data": {
        "id": 1,
        "name": "Product A",
        "price": 1000,
        "stock": 50
      }
    }
    ```

  - **Not Found:** `404 Not Found`

    ```json
    {
      "code": 404,
      "message": "PRODUCT_NOT_FOUND"
    }
    ```

#### 3. 모든 상품 목록 조회

- **URL:** `/api/v1/product/all`
- **Method:** `GET`
- **Description:** 모든 상품 목록을 조회합니다.
- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "SUCCESS",
      "data": [
        {
          "id": 1,
          "name": "Product A",
          "price": 1000,
          "stock": 50
        },
        {
          "id": 2,
          "name": "Product B",
          "price": 2000,
          "stock": 30
        }
      ]
    }
    ```

#### 4. 구매 가능한 상품 목록 조회 (Paginated)

- **URL:** `/api/v1/product/available`
- **Method:** `GET`
- **Description:** 재고가 있는 구매 가능한 상품 목록을 페이징하여 조회합니다.
- **Query Parameters:**

  | Parameter | Type | Default | Description             |
  |-----------|------|---------|-------------------------|
  | `page`    | int  | 0       | 페이지 번호 (0부터 시작) |
  | `size`    | int  | 10      | 페이지당 아이템 수        |

- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "SUCCESS",
      "data": {
        "content": [
          {
            "id": 1,
            "name": "Product A",
            "price": 1000,
            "stock": 50
          },
          {
            "id": 2,
            "name": "Product B",
            "price": 2000,
            "stock": 30
          }
        ],
        "pageNumber": 0,
        "pageSize": 10,
        "totalElements": 2,
        "totalPages": 1,
        "last": true
      }
    }
    ```

#### 5. 상품 업데이트

- **URL:** `/api/v1/product`
- **Method:** `PATCH`
- **Description:** 특정 상품의 정보를 업데이트합니다.
- **Request Body:**

  ```json
  {
    "productId": 1,
    "name": "Updated Product A",
    "price": 1500,
    "stock": 40
  }
  ```

- **필드 설명:**
  - `productId` (long, 필수): 업데이트할 상품의 ID
  - `name` (String, 선택): 상품명
  - `price` (Long, 선택): 상품 가격
  - `stock` (Integer, 선택): 재고 수량

- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "UPDATE"
    }
    ```

  - **Not Found:** `404 Not Found`

    ```json
    {
      "code": 404,
      "message": "PRODUCT_NOT_FOUND"
    }
    ```

#### 6. 상품 삭제

- **URL:** `/api/v1/product`
- **Method:** `DELETE`
- **Description:** 특정 상품을 삭제합니다.
- **Query Parameters:**

  | Parameter   | Type   | Description     |
  |-------------|--------|-----------------|
  | `productId` | `long` | 삭제할 상품의 ID |

- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "PRODUCT_DELETED"
    }
    ```

  - **Not Found:** `404 Not Found`

    ```json
    {
      "code": 404,
      "message": "PRODUCT_NOT_FOUND"
    }
    ```

---

### Cart API

`/api/v1/cart`

#### 1. 장바구니 항목 조회

- **URL:** `/api/v1/cart/{customerId}`
- **Method:** `GET`
- **Description:** 특정 고객의 장바구니 항목을 조회합니다.
- **Path Parameters:**

  | Parameter    | Type   | Description      |
  |--------------|--------|------------------|
  | `customerId` | `Long` | 조회할 고객의 ID |

- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "SUCCESS",
      "data": {
        "customerId": 1,
        "items": [
          {
            "productId": 1,
            "productName": "Product A",
            "quantity": 2,
            "price": 1000
          },
          {
            "productId": 2,
            "productName": "Product B",
            "quantity": 1,
            "price": 2000
          }
        ]
      }
    }
    ```

  - **Not Found:** `404 Not Found`

    ```json
    {
      "code": 404,
      "message": "CART_NOT_FOUND"
    }
    ```

#### 2. 장바구니에 추가, 변경, 제거

- **URL:** `/api/v1/cart`
- **Method:** `POST`
- **Description:** 특정 고객의 장바구니에 상품을 추가, 수량의 변경, 카트 내역에서 제거를 합니다.
- **Request Body:**

  ```json
  {
    "customerId": 1,
    "productId": 1,
    "quantity": 2
  }
  ```

- **필드 설명:**
  - `customerId` (Long, 필수): 장바구니 소유자의 고객 ID
  - `productId` (Long, 필수): 추가할 상품의 ID
  - `quantity` (Integer, 필수): 추가할 상품의 수량 (- 수량 입력시 카트 수량 감소, 0이 되면 카트에서 상품 제거)

- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "SUCCESS"
    }
    ```

  - **Not Found:** `404 Not Found`

    ```json
    {
      "code": 404,
      "message": "PRODUCT_NOT_FOUND"
    }
    ```

#### 3. 장바구니 비우기

- **URL:** `/api/v1/cart/{customerId}`
- **Method:** `DELETE`
- **Description:** 특정 고객의 장바구니를 비웁니다.
- **Path Parameters:**

  | Parameter    | Type   | Description         |
  |--------------|--------|---------------------|
  | `customerId` | `Long` | 비울 장바구니 소유자의 고객 ID |

- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "CART_CLEARED"
    }
    ```

  - **Not Found:** `404 Not Found`

    ```json
    {
      "code": 404,
      "message": "CART_NOT_FOUND"
    }
    ```

---

### Order API

`/api/v1/orders`

#### 1. 주문 상세 조회

- **URL:** `/api/v1/orders/{orderId}`
- **Method:** `GET`
- **Description:** 특정 주문의 상세 정보를 조회합니다.
- **Path Parameters:**

  | Parameter | Type   | Description      |
  |-----------|--------|------------------|
  | `orderId` | `Long` | 조회할 주문의 ID |

- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "SUCCESS",
      "data": {
        "orderId": 1001,
        "customerId": 1,
        "orderDate": "2024-12-21T10:00:00",
        "status": "PAID",
        "totalAmount": 3000,
        "orderItems": [
          {
            "productId": 1,
            "productName": "Product A",
            "quantity": 2,
            "price": 1000
          },
          {
            "productId": 2,
            "productName": "Product B",
            "quantity": 1,
            "price": 2000
          }
        ]
      }
    }
    ```

  - **Not Found:** `404 Not Found`

    ```json
    {
      "code": 404,
      "message": "ORDER_NOT_FOUND"
    }
    ```

---

### Payment API

`/api/v1/payment`

#### 1. 주문 생성 및 결제

- **URL:** `/api/v1/payment`
- **Method:** `POST`
- **Description:** 새로운 주문을 생성하고 결제합니다.
- **Request Body:**

  ```json
  {
    "customerId": 1
  }
  ```

- **필드 설명:**
  - `customerId` (Long, 필수): 주문을 생성할 고객의 ID

- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "SUCCESS",
      "data": {
        "customerId": 1,
        "customerName": "tester",
        "orderId": 1001,
        "transactionId": "TX12345",
        "status": "PAID",
        "message": "Payment successful",
        "totalPrice": 3000,
        "orderItems": [
            {
                "productId": 3,
                "productName": "갤럭시 버즈",
                "productPrice": 200000,
                "quantity": 1
            }
        ]
      }
    }
    ```

  - **Not Found:** `404 Not Found`

    ```json
    {
      "code": 404,
      "message": "CUSTOMER_NOT_FOUND"
    }
    ```
    
  - **PAYMENT SERVER ERROR:** ``

    ```json
    {
        "code": 503,
        "message": "Payment server error",
        "data": {
            "customerId": 1,
            "customerName": "test1",
            "orderId": 39,
            "status": "FAILED",
            "message": "something wrong!"
        }
    }
    ```

#### 2. 결제 상세 조회

- **URL:** `/api/v1/payment/{orderId}`
- **Method:** `GET`
- **Description:** 특정 주문의 결제 상세 정보를 조회합니다.
- **Path Parameters:**

  | Parameter | Type   | Description      |
  |-----------|--------|------------------|
  | `orderId` | `Long` | 조회할 주문의 ID |

- **Responses:**
  - **Success:** `200 OK`

    ```json
    {
      "code": 200,
      "message": "SUCCESS",
      "data": {
        "paymentId": 5001,
        "transactionId": "TX12345",
        "status": "SUCCESS",
        "message": "Payment successful",
        "paymentDate": "2024-12-21T10:05:00",
        "order": {
            "orderId": 30,
            "orderStatus": "PAID",
            "totalAmount": 400000,
            "orderItems": [
                {
                    "productId": 3,
                    "productName": "갤럭시 버즈",
                    "productPrice": 200000,
                    "quantity": 2
                }
            ]
        }
      }
    }
    ```

  - **Not Found:** `404 Not Found`

    ```json
    {
      "code": 404,
      "message": "PAYMENT_NOT_FOUND"
    }
    ```

---

## Error Handling

모든 API는 일관된 오류 응답 구조를 사용합니다. 오류가 발생하면 `CommonResponse.fail` 메서드를 통해 오류 코드와 메시지를 반환합니다.

### 오류 응답 구조

```json
{
  "code": 400,
  "message": "VALIDATION_ERROR"
  "details": "{customerId=널이어서는 안됩니다}"
}
```

> **설명:**
>
> - `code`: HTTP 상태 코드 (예: `200`, `400`, `404`)
> - `message`: 오류의 유형을 나타내는 메시지 (예: `SUCCESS`, `VALIDATION_ERROR`)
> - `detail`: 오류와 관련된 추가 데이터를 포함할 수 있지만, 기본적으로 `null`입니다.

### 공통 코드

| 코드             | 코드명                | 설명                                      |
|---------------------|---------------------|-------------------------------------------|
|`200`| `SUCCESS`               | 요청이 성공적으로 처리되었습니다.         |
|`201`| `CREATED`               | 데이터가 성공적으로 생성되었습니다.       |
|`200`| `CART_CLEARED`          | 장바구니가 성공적으로 비워졌습니다.       |
|`200`| `UPDATE`                | 데이터가 성공적으로 업데이트되었습니다.   |
|`200`| `PRODUCT_DELETED`       | 상품이 성공적으로 삭제되었습니다.         |
|`400`| `VALIDATE`              | 요청 데이터의 유효성 검사에 실패했습니다. |
|`400`| `NOT_FOUND`             | 자원이 존재하지 않습니다.                 |
|`400`| `VALIDATION_ERROR`      | 요청 데이터의 유효성 검사에 실패했습니다. |
|`404`| `PRODUCT_NOT_FOUND`     | 지정한 상품을 찾을 수 없습니다.           |
|`404`| `ORDER_NOT_FOUND`       | 지정한 주문을 찾을 수 없습니다.           |
|`404`| `PAYMENT_NOT_FOUND`     | 지정한 결제 정보를 찾을 수 없습니다.       |
|`409`| `CART_NOT_FOUND`        | 지정한 고객의 장바구니를 찾을 수 없습니다. |
|`409`| `CONFILCT`              | 충돌이 발생했습니다.                      |
|`500`| `INTERNAL_SERVER_ERROR` | 내부서버오류가 발생했습니다.              |
|`503`| `PAYMENT_SERVER_ERROR`  | 결제요청에 실패하였습니다.                |

---

## Examples

### 1. 상품 등록

- **Request:**

  ```http
  POST /api/v1/product
  Content-Type: application/json

  [
    {
      "name": "Product A",
      "price": 1000,
      "stock": 50
    },
    {
      "name": "Product B",
      "price": 2000,
      "stock": 30
    }
  ]
  ```

- **Response:**

  ```json
  {
    "code": 200,
    "message": "SUCCESS"
  }
  ```

### 2. 구매 가능한 상품 목록 조회

- **Request:**

  ```http
  GET /api/v1/product/available?page=0&size=10
  ```

- **Response:**

  ```json
  {
    "code": 200,
    "message": "SUCCESS",
    "data": {
      "content": [
        {
          "id": 1,
          "name": "Product A",
          "price": 1000,
          "stock": 50
        },
        {
          "id": 2,
          "name": "Product B",
          "price": 2000,
          "stock": 30
        }
      ],
      "pageNumber": 0,
      "pageSize": 10,
      "totalElements": 2,
      "totalPages": 1,
      "last": true
    }
  }
  ```

---

## ERD
![test_db1](https://github.com/user-attachments/assets/6d570524-b57a-4a70-bab6-1a4f4bd2c049)
