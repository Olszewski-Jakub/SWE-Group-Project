Test-Support Module

Purpose
- Shared utilities for tests, focused on MVC slice testing.

Key Utilities
- `@DeliveryWebMvcTest`: meta-annotation wrapping `@WebMvcTest` and minimal `TestBoot` config.
- `CommonWebMvcTest`: base class wiring `MockMvc` and `ObjectMapper` with JSON helpers.
- `TestBoot`: lean Boot config scanning only required packages for faster tests.

Usage
- Annotate controller tests with `@DeliveryWebMvcTest(controllers = YourController.class)`.
- Extend `CommonWebMvcTest` to get `mockMvc` and `objectMapper`.

