/**
 * CHATBOT WIDGET – FINAL DEFINITIVO
 */

// ============================================================================
// CONFIG
// ============================================================================

const CHATBOT_CONFIG = {
    apiUrl: '/api/chatbot/message',
    typingDelay: 600
};

// ============================================================================
// ESTADO
// ============================================================================

let chatState = {
    sessionId: generateSessionId()
};

let waitingForUserName = false;

// ============================================================================
// USUARIO
// ============================================================================

function getUserName() {
    return localStorage.getItem('chatbotUserName');
}

function setUserName(name) {
    localStorage.setItem('chatbotUserName', name);
}

// ============================================================================
// SONIDO
// ============================================================================

function playSound(id) {
    const sound = document.getElementById(id);
    if (!sound) return;
    sound.volume = 0.3;
    sound.currentTime = 0;
    sound.play().catch(() => {});
}

// ============================================================================
// MENSAJES
// ============================================================================

async function sendMessage() {
    const input = document.getElementById('user-input');
    const message = input.value.trim();
    if (!message) return;

    addUserMessage(message);
    input.value = '';
    hideQuickOptions();

    // ===============================
    // CAPTURAR NOMBRE
    // ===============================
    if (waitingForUserName) {
        setUserName(message);
        waitingForUserName = false;

        showTypingIndicator();

        setTimeout(async () => {
            hideTypingIndicator();

            addBotMessage({
                message: `😊 ¡Encantado de conocerte, <strong>${message}</strong>!<br>
                          ¿En qué puedo ayudarte hoy?`
            });

            showTypingIndicator();
            await sendIntent('MENU', null);

        }, 600);

        return;
    }

    // ===============================
    // FLUJO NORMAL
    // ===============================
    showTypingIndicator();
    await sendIntent(null, message);
}

function sendQuickOption(intent) {
    hideQuickOptions();
    showTypingIndicator();
    sendIntent(intent, null);
}

async function sendIntent(intent, message) {
    try {
        const response = await fetch(CHATBOT_CONFIG.apiUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                intent,
                message,
                sessionId: chatState.sessionId
            })
        });

        const data = await response.json();

        setTimeout(() => {
            hideTypingIndicator();
            addBotMessage(data);
        }, CHATBOT_CONFIG.typingDelay);

    } catch {
        hideTypingIndicator();
        addBotMessage({ message: '😔 Ha ocurrido un error. Inténtalo de nuevo.' });
    }
}

// ============================================================================
// UI MENSAJES
// ============================================================================

function addUserMessage(message) {
    const messages = document.getElementById('chatbot-messages');
    messages.insertAdjacentHTML('beforeend', `
        <div class="message user-message">
            <div class="message-content">${escapeHtml(message)}</div>
            <span class="message-time">${getCurrentTime()}</span>
        </div>
    `);
    scrollToBottom();
}

function addBotMessage(data) {
    const messages = document.getElementById('chatbot-messages');

    if (data.message) {
        playSound('chat-message-sound');
        messages.insertAdjacentHTML('beforeend', `
            <div class="message bot-message">
                <div class="message-content">${formatMessage(data.message)}</div>
                <span class="message-time">${getCurrentTime()}</span>
            </div>
        `);
    }

    if (data.formUrl || data.redirectUrl) {
        sessionStorage.setItem('reopenChatbot', 'true');
        sessionStorage.setItem('returnToMenu', 'true');
        setTimeout(() => {
            window.location.href = data.formUrl || data.redirectUrl;
        }, 800);
        return;
    }

    if (data.options) addQuickOptions(data.options);
    scrollToBottom();
}

// ============================================================================
// OPCIONES
// ============================================================================

function addQuickOptions(options) {
    const messages = document.getElementById('chatbot-messages');
    let html = '<div class="quick-options">';
    options.forEach(o => {
        html += `<button class="option-btn" onclick="sendQuickOption('${o.value}')">${escapeHtml(o.label)}</button>`;
    });
    html += '</div>';
    messages.insertAdjacentHTML('beforeend', html);
    scrollToBottom();
}

function hideQuickOptions() {
    document.querySelectorAll('.quick-options').forEach(el => el.style.display = 'none');
}

// ============================================================================
// TYPING
// ============================================================================

function showTypingIndicator() {
    const messages = document.getElementById('chatbot-messages');
    messages.insertAdjacentHTML('beforeend', `
        <div class="message bot-message typing-message">
            <div class="typing-indicator"><span></span><span></span><span></span></div>
        </div>
    `);
}

function hideTypingIndicator() {
    const el = document.querySelector('.typing-message');
    if (el) el.remove();
}

// ============================================================================
// TOGGLE CHAT
// ============================================================================

function toggleChat() {
    const chatWindow = document.getElementById('chatbot-window');
    if (!chatWindow) return;

    const isOpen = chatWindow.classList.contains('active');

    if (isOpen) {
        chatWindow.classList.remove('active');
        setTimeout(() => chatWindow.style.display = 'none', 300);
    } else {
        chatWindow.style.display = 'flex';
        setTimeout(() => chatWindow.classList.add('active'), 10);
        playSound('chat-open-sound');

        if (!getUserName() && !waitingForUserName) {
            askUserName();
        }

        setTimeout(() => document.getElementById('user-input')?.focus(), 200);
    }
}

// ============================================================================
// PEDIR NOMBRE
// ============================================================================

function askUserName() {
    waitingForUserName = true;
    const messages = document.getElementById('chatbot-messages');
    messages.insertAdjacentHTML('beforeend', `
        <div class="message bot-message">
            <div class="message-content">
                👋 ¡Hola! Antes de empezar,<br>
                <strong>¿cómo te llamas?</strong>
            </div>
            <span class="message-time">${getCurrentTime()}</span>
        </div>
    `);
    scrollToBottom();
}

// ============================================================================
// REAPERTURA
// ============================================================================

document.addEventListener('DOMContentLoaded', async () => {
    if (sessionStorage.getItem('reopenChatbot') === 'true') {
        sessionStorage.removeItem('reopenChatbot');
        const chatWindow = document.getElementById('chatbot-window');
        chatWindow.style.display = 'flex';
        setTimeout(() => chatWindow.classList.add('active'), 10);

        if (getUserName()) {
            showTypingIndicator();
            await sendIntent('MENU', null);
        }
    }
});

// ============================================================================
// UTILS
// ============================================================================

function scrollToBottom() {
    const messages = document.getElementById('chatbot-messages');
    messages.scrollTop = messages.scrollHeight;
}

function formatMessage(t) {
    return t.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>').replace(/\n/g, '<br>');
}

function escapeHtml(t) {
    const d = document.createElement('div');
    d.textContent = t;
    return d.innerHTML;
}

function getCurrentTime() {
    const d = new Date();
    return d.getHours().toString().padStart(2, '0') + ':' +
        d.getMinutes().toString().padStart(2, '0');
}

function generateSessionId() {
    return 'session_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
}

// ============================================================================
// EXPORTS
// ============================================================================

window.sendMessage = sendMessage;
window.sendQuickOption = sendQuickOption;
window.toggleChat = toggleChat;
