const INTELLIGENCE_BASE = `${API_BASE}/intelligence`;

async function getLifecycle(productId) {
    const response = await fetch(`${INTELLIGENCE_BASE}/products/${productId}/lifecycle`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getTimeline(productId) {
    const response = await fetch(`${INTELLIGENCE_BASE}/products/${productId}/timeline`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getWarrantyIntelligence(productId) {
    const response = await fetch(`${INTELLIGENCE_BASE}/products/${productId}/warranty-intelligence`, {
        credentials: "include"
    });
    if (response.status === 204) return null;
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getClaimReadiness(productId) {
    const response = await fetch(`${INTELLIGENCE_BASE}/products/${productId}/claim-readiness`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getOwnershipCost(productId) {
    const response = await fetch(`${INTELLIGENCE_BASE}/products/${productId}/ownership-cost`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getProductHealth(productId) {
    const response = await fetch(`${INTELLIGENCE_BASE}/products/${productId}/health`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getProductStatus(productId) {
    const response = await fetch(`${INTELLIGENCE_BASE}/products/${productId}/status`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getInsights() {
    const response = await fetch(`${API_BASE}/dashboard/insights`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getWarrantyOverview() {
    const response = await fetch(`${API_BASE}/dashboard/warranty-overview`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getServiceAnalytics() {
    const response = await fetch(`${API_BASE}/dashboard/service-analytics`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getRecentActivity() {
    const response = await fetch(`${API_BASE}/dashboard/activity`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getHealthOverview() {
    const response = await fetch(`${API_BASE}/dashboard/health`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function search(q) {
    const response = await fetch(`${API_BASE}/search?q=${encodeURIComponent(q)}`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getNotifications() {
    const response = await fetch(`${API_BASE}/notifications`, {
        credentials: "include"
    });
    if (!response.ok) throw new Error(await getApiErrorMessage(response));
    return response.json();
}

async function getApiErrorMessage(response) {
    const error = await response.json().catch(() => null);
    return error?.message || "Something went wrong. Please try again.";
}
