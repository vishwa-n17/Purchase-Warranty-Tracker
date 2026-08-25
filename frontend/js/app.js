const healthStatus = document.getElementById("health-status");
const checkStatusButton = document.getElementById("check-status-button");
const healthEndpoint = "http://localhost:8080/api/health";

async function checkBackendHealth() {
    healthStatus.textContent = "Checking the backend…";

    try {
        const response = await fetch(healthEndpoint);
        if (!response.ok) {
            throw new Error(`Request failed with status ${response.status}`);
        }

        const health = await response.json();
        healthStatus.textContent = `${health.status}: ${health.message}`;
    } catch (error) {
        healthStatus.textContent = "Backend is unavailable. Start the Spring Boot application and try again.";
        console.error("Unable to reach the health endpoint:", error);
    }
}

checkStatusButton.addEventListener("click", checkBackendHealth);
checkBackendHealth();
