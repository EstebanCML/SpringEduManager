(function () {
    const FORM_SELECTOR = "form[data-confirm-delete='true']";
    const DEFAULT_MESSAGE = "¿Confirmas esta eliminación?";

    function createModal() {
        const wrapper = document.createElement("div");
        wrapper.innerHTML = `
            <div class="modal fade" id="confirmDeleteModal" tabindex="-1" aria-labelledby="confirmDeleteModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content bg-dark text-light border-secondary">
                        <div class="modal-header border-secondary">
                            <h5 class="modal-title" id="confirmDeleteModalLabel">Confirmar eliminación</h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Cerrar"></button>
                        </div>
                        <div class="modal-body" id="confirmDeleteModalBody"></div>
                        <div class="modal-footer border-secondary">
                            <button type="button" class="btn btn-outline-light btn-sm" data-bs-dismiss="modal">Cancelar</button>
                            <button type="button" class="btn btn-danger btn-sm" id="confirmDeleteModalAccept">Eliminar</button>
                        </div>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(wrapper.firstElementChild);
        return document.getElementById("confirmDeleteModal");
    }

    function initDeleteConfirmations() {
        const forms = document.querySelectorAll(FORM_SELECTOR);
        if (!forms.length) return;

        if (typeof bootstrap === "undefined" || !bootstrap.Modal) {
            forms.forEach((form) => {
                form.addEventListener("submit", (event) => {
                    const message = form.getAttribute("data-confirm-message") || DEFAULT_MESSAGE;
                    if (!window.confirm(message)) {
                        event.preventDefault();
                    }
                });
            });
            return;
        }

        const modalElement = createModal();
        const modalBody = modalElement.querySelector("#confirmDeleteModalBody");
        const acceptButton = modalElement.querySelector("#confirmDeleteModalAccept");
        const modal = new bootstrap.Modal(modalElement);
        let targetForm = null;

        forms.forEach((form) => {
            form.addEventListener("submit", (event) => {
                if (form.dataset.confirmed === "true") {
                    form.dataset.confirmed = "false";
                    return;
                }
                event.preventDefault();
                targetForm = form;
                modalBody.textContent = form.getAttribute("data-confirm-message") || DEFAULT_MESSAGE;
                modal.show();
            });
        });

        acceptButton.addEventListener("click", () => {
            if (!targetForm) return;
            targetForm.dataset.confirmed = "true";
            modal.hide();
            targetForm.requestSubmit();
        });
    }

    document.addEventListener("DOMContentLoaded", initDeleteConfirmations);
})();
