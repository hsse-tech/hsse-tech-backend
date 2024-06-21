package com.mipt.hsse.hssetechbackend.oauth;

public class PhystechDomainValidator {
    private static final String PHYSTECH_DOMAIN = "phystech.edu";

    /**
     * Проверяет валидность email-а. В данной функции, мы считаем email идеальным (то есть совпадает со своим стандартным RegEx-ом)
     */
    public static boolean isValid(String email) {
        // PS: предвижу возмущение: "А где RegEx?!!!!". А я отвечу, что это решение работает за чистый O(n) в отличие от O(e^n)
        var i = 0;
        for (i = 0; i < email.length(); i++) {
            if (email.charAt(i) == '@') {
                i++;
                break;
            }
        }

        var offset = i;

        var countMatched = 0;

        for (; (i < email.length()) && (i - offset < PHYSTECH_DOMAIN.length()); i++) {
            if (email.charAt(i) == PHYSTECH_DOMAIN.charAt(i - offset)) {
                countMatched++;
            }
        }

        return countMatched == PHYSTECH_DOMAIN.length() && i == email.length();
    }
}
