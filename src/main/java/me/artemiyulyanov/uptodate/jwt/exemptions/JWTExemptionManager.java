package me.artemiyulyanov.uptodate.jwt.exemptions;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class JWTExemptionManager {
    private List<Predicate<HttpServletRequest>> exemptRequests = new ArrayList<>();

    public boolean isRequestExempt(HttpServletRequest request) {
        for (Predicate<HttpServletRequest> predicate : exemptRequests) {
            if (predicate.test(request)) return true;
        }

        return false;
    }

    public JWTExemptionManager exempt(Predicate<HttpServletRequest> predication) {
        exemptRequests.add(predication);
        return this;
    }
}