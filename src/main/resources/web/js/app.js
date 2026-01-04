// AethorQuests Web Panel - Main Application
class AethorQuestsApp {
    constructor() {
        this.token = localStorage.getItem('authToken');
        this.username = localStorage.getItem('username');
        this.quests = [];
        this.npcs = [];
        this.selectedNpc = null;
        
        this.init();
    }
    
    init() {
        if (this.token) {
            this.verifyAuth().then(valid => {
                if (valid) {
                    this.showDashboard();
                } else {
                    this.showLogin();
                }
            });
        } else {
            this.showLogin();
        }
        
        this.setupEventListeners();
    }
    
    setupEventListeners() {
        // Login form
        document.getElementById('loginForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.handleLogin();
        });
        
        // Logout button
        document.getElementById('logoutBtn').addEventListener('click', () => this.handleLogout());
        
        // Navigation
        document.querySelectorAll('.nav-item').forEach(item => {
            item.addEventListener('click', (e) => {
                const view = e.target.dataset.view;
                this.switchView(view);
            });
        });
        
        // Create quest button
        document.getElementById('createQuestBtn').addEventListener('click', () => this.openQuestEditor());
        
        // Quest editor form
        document.getElementById('questEditorForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveQuest();
        });
        
        // Dynamic list buttons
        document.getElementById('addObjectiveBtn').addEventListener('click', () => this.addObjectiveField());
        document.getElementById('addItemRewardBtn').addEventListener('click', () => this.addItemRewardField());
        document.getElementById('addCommandBtn').addEventListener('click', () => this.addCommandField());
        
        // Modal close buttons
        document.querySelectorAll('.modal-close').forEach(btn => {
            btn.addEventListener('click', () => this.closeModal());
        });
        
        // Quest search
        document.getElementById('questSearch').addEventListener('input', (e) => {
            this.filterQuests(e.target.value);
        });
    }
    
    async verifyAuth() {
        try {
            const response = await fetch('/api/auth/verify', {
                headers: { 'Authorization': this.token }
            });
            const data = await response.json();
            return data.valid;
        } catch (error) {
            return false;
        }
    }
    
    async handleLogin() {
        const username = document.getElementById('username').value;
        const password = document.getElementById('password').value;
        const errorEl = document.getElementById('loginError');
        
        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username, password })
            });
            
            const data = await response.json();
            
            if (data.success) {
                this.token = data.token;
                this.username = data.username;
                localStorage.setItem('authToken', this.token);
                localStorage.setItem('username', this.username);
                this.showDashboard();
            } else {
                errorEl.textContent = data.error || 'Login failed';
                errorEl.classList.add('show');
            }
        } catch (error) {
            errorEl.textContent = 'Connection error. Make sure the server is running.';
            errorEl.classList.add('show');
        }
    }
    
    async handleLogout() {
        await fetch('/api/auth/logout', {
            method: 'POST',
            headers: { 'Authorization': this.token }
        });
        
        localStorage.removeItem('authToken');
        localStorage.removeItem('username');
        this.token = null;
        this.username = null;
        this.showLogin();
    }
    
    showLogin() {
        document.getElementById('loginScreen').style.display = 'block';
        document.getElementById('dashboard').style.display = 'none';
    }
    
    showDashboard() {
        document.getElementById('loginScreen').style.display = 'none';
        document.getElementById('dashboard').style.display = 'block';
        document.getElementById('username-display').textContent = this.username;
        
        this.loadQuests();
        this.loadNpcs();
    }
    
    switchView(viewName) {
        // Update nav items
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.remove('active');
            if (item.dataset.view === viewName) {
                item.classList.add('active');
            }
        });
        
        // Update views
        document.querySelectorAll('.view').forEach(view => {
            view.classList.remove('active');
        });
        document.getElementById(viewName + 'View').classList.add('active');
        
        // Load data for specific views
        if (viewName === 'assignment') {
            this.loadAssignmentView();
        }
    }
    
    async loadQuests() {
        try {
            const response = await fetch('/api/quests', {
                headers: { 'Authorization': this.token }
            });
            const data = await response.json();
            this.quests = data.quests || [];
            this.renderQuests();
        } catch (error) {
            console.error('Failed to load quests:', error);
        }
    }
    
    renderQuests(filter = '') {
        const container = document.getElementById('questsList');
        const filtered = this.quests.filter(q => 
            q.title.toLowerCase().includes(filter.toLowerCase()) ||
            q.id.toLowerCase().includes(filter.toLowerCase())
        );
        
        if (filtered.length === 0) {
            container.innerHTML = '<p style="color: #94a3b8; text-align: center;">No quests found</p>';
            return;
        }
        
        container.innerHTML = filtered.map(quest => `
            <div class="quest-card">
                <div class="card-header">
                    <div>
                        <div class="card-title">${quest.title}</div>
                        <div class="card-subtitle">ID: ${quest.id}</div>
                    </div>
                    <div class="card-actions">
                        <button class="icon-btn" onclick="app.editQuest('${quest.id}')">✏️ Edit</button>
                        <button class="icon-btn" onclick="app.deleteQuest('${quest.id}')" style="border-color: #ef4444;">🗑️</button>
                    </div>
                </div>
                <div class="card-content">
                    <p>${quest.description[0] || 'No description'}</p>
                    <div style="margin-top: 12px;">
                        <span class="badge badge-primary">NPC: ${quest.giverNpcId}</span>
                        <span class="badge badge-success">${quest.objectives?.length || 0} Objectives</span>
                        <span class="badge badge-warning">Level ${quest.minLevel || 1}+</span>
                    </div>
                </div>
            </div>
        `).join('');
    }
    
    filterQuests(term) {
        this.renderQuests(term);
    }
    
    async loadNpcs() {
        try {
            const response = await fetch('/api/npcs', {
                headers: { 'Authorization': this.token }
            });
            const data = await response.json();
            this.npcs = data.npcs || [];
            this.renderNpcs();
        } catch (error) {
            console.error('Failed to load NPCs:', error);
        }
    }
    
    renderNpcs() {
        const container = document.getElementById('npcsList');
        
        if (this.npcs.length === 0) {
            container.innerHTML = '<p style="color: #94a3b8; text-align: center;">No NPCs found</p>';
            return;
        }
        
        container.innerHTML = this.npcs.map(npc => `
            <div class="npc-card">
                <div class="card-header">
                    <div>
                        <div class="card-title">${npc.displayName || npc.id}</div>
                        <div class="card-subtitle">ID: ${npc.id}</div>
                    </div>
                </div>
                <div class="card-content">
                    <span class="badge badge-success">${npc.questCount} Quests</span>
                    <span class="badge ${npc.spawned ? 'badge-success' : 'badge-warning'}">
                        ${npc.spawned ? '✓ Spawned' : '✗ Not Spawned'}
                    </span>
                </div>
            </div>
        `).join('');
    }
    
    loadAssignmentView() {
        const npcsList = document.getElementById('assignmentNpcsList');
        
        npcsList.innerHTML = this.npcs.map(npc => `
            <div class="npc-item" onclick="app.selectNpcForAssignment('${npc.id}')">
                <strong>${npc.displayName || npc.id}</strong><br>
                <small style="color: #94a3b8;">${npc.questCount} quests</small>
            </div>
        `).join('');
    }
    
    async selectNpcForAssignment(npcId) {
        this.selectedNpc = npcId;
        
        // Highlight selected NPC
        document.querySelectorAll('.npc-item').forEach(item => {
            item.classList.remove('selected');
        });
        event.target.closest('.npc-item').classList.add('selected');
        
        // Load quests for NPC
        try {
            const response = await fetch(`/api/npcs/${npcId}/quests`, {
                headers: { 'Authorization': this.token }
            });
            const data = await response.json();
            const npcQuests = data.quests || [];
            
            document.getElementById('selectedNpcName').textContent = npcId;
            
            const questsList = document.getElementById('assignmentQuestsList');
            questsList.innerHTML = this.quests.map(quest => {
                const assigned = quest.giverNpcId === npcId;
                return `
                    <div class="quest-item" style="display: flex; justify-content: space-between; align-items: center;" onclick="app.toggleQuestAssignment('${quest.id}', '${npcId}')">
                        <div>
                            <strong>${quest.title}</strong><br>
                            <small style="color: #94a3b8;">${quest.id}</small>
                        </div>
                        <span class="badge ${assigned ? 'badge-success' : 'badge-warning'}">
                            ${assigned ? '✓ Assigned' : '✗ Not Assigned'}
                        </span>
                    </div>
                `;
            }).join('');
        } catch (error) {
            console.error('Failed to load NPC quests:', error);
        }
    }
    
    async toggleQuestAssignment(questId, npcId) {
        const quest = this.quests.find(q => q.id === questId);
        if (!quest) return;
        
        // Toggle assignment
        quest.giverNpcId = quest.giverNpcId === npcId ? 'none' : npcId;
        
        try {
            await fetch(`/api/quests/${questId}`, {
                method: 'PUT',
                headers: {
                    'Authorization': this.token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(quest)
            });
            
            // Reload views
            await this.loadQuests();
            await this.loadNpcs();
            this.selectNpcForAssignment(npcId);
        } catch (error) {
            console.error('Failed to update quest assignment:', error);
            alert('Failed to update assignment');
        }
    }
    
    openQuestEditor(questId = null) {
        const modal = document.getElementById('questEditorModal');
        const form = document.getElementById('questEditorForm');
        
        // Clear dynamic fields first
        document.getElementById('objectivesList').innerHTML = '';
        document.getElementById('itemRewardsList').innerHTML = '';
        document.getElementById('commandsList').innerHTML = '';
        
        if (questId) {
            const quest = this.quests.find(q => q.id === questId);
            if (quest) {
                console.log('Editing quest:', quest);
                console.log('Quest objectives:', quest.objectives);
                console.log('Quest rewards:', quest.rewards);
                
                document.getElementById('editorTitle').textContent = 'Edit Quest';
                document.getElementById('questId').value = quest.id;
                document.getElementById('questId').disabled = true;
                document.getElementById('questTitle').value = quest.title;
                document.getElementById('questNpcId').value = quest.giverNpcId;
                document.getElementById('questDescription').value = quest.description.join('\n');
                document.getElementById('questMinLevel').value = quest.minLevel || 1;
                
                // Populate objectives
                console.log('About to add', (quest.objectives || []).length, 'objectives');
                (quest.objectives || []).forEach((obj, idx) => {
                    console.log(`Adding objective ${idx}:`, obj);
                    this.addObjectiveField(obj);
                });
                
                // Populate rewards
                document.getElementById('rewardXp').value = quest.rewards?.xp || 0;
                document.getElementById('rewardMoney').value = quest.rewards?.money || 0;
                (quest.rewards.items || []).forEach(item => this.addItemRewardField(item));
                (quest.rewards.commands || []).forEach(cmd => this.addCommandField(cmd));
            }
        } else {
            form.reset();
            document.getElementById('editorTitle').textContent = 'Create Quest';
            document.getElementById('questId').disabled = false;
        }
        
        modal.classList.add('show');
    }
    
    closeModal() {
        document.querySelectorAll('.modal').forEach(modal => {
            modal.classList.remove('show');
        });
    }
    
    async saveQuest() {
        const id = document.getElementById('questId').value;
        const isEdit = this.quests.some(q => q.id === id);
        
        try {
            // Collect objectives
            const objectives = [];
            document.querySelectorAll('#objectivesList .objective-item').forEach(item => {
                const type = item.querySelector('.obj-type').value;
                const objective = { type };
                
                if (type === 'KILL') {
                    objective.targetId = item.querySelector('.obj-target').value;
                    objective.required = parseInt(item.querySelector('.obj-amount').value) || 1;
                } else if (type === 'TALK') {
                    objective.targetId = item.querySelector('.obj-target').value;
                } else if (type === 'COLLECT') {
                    objective.targetId = item.querySelector('.obj-target').value;
                    objective.required = parseInt(item.querySelector('.obj-amount').value) || 1;
                } else if (type === 'VISIT') {
                    objective.targetId = item.querySelector('.obj-target').value;
                }
                
                objectives.push(objective);
            });
            
            // Collect item rewards
            const items = [];
            document.querySelectorAll('#itemRewardsList .item-reward-item').forEach(item => {
                items.push({
                    type: item.querySelector('.item-material').value,
                    amount: parseInt(item.querySelector('.item-amount').value) || 1
                });
            });
            
            // Collect command rewards
            const commands = [];
            document.querySelectorAll('#commandsList .command-item').forEach(item => {
                const cmd = item.querySelector('.command-input').value.trim();
                if (cmd) commands.push(cmd);
            });
            
            const quest = {
                id: id,
                title: document.getElementById('questTitle').value,
                giverNpcId: document.getElementById('questNpcId').value,
                description: document.getElementById('questDescription').value.split('\n').filter(l => l.trim()),
                minLevel: parseInt(document.getElementById('questMinLevel').value) || 1,
                objectives: objectives,
                rewards: {
                    xp: parseInt(document.getElementById('rewardXp').value) || 0,
                    money: parseFloat(document.getElementById('rewardMoney').value) || 0,
                    items: items,
                    commands: commands
                }
            };
            
            const url = isEdit ? `/api/quests/${id}` : '/api/quests';
            const method = isEdit ? 'PUT' : 'POST';
            
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Authorization': this.token,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(quest)
            });
            
            if (response.ok) {
                this.closeModal();
                await this.loadQuests();
                await this.loadNpcs();
            } else {
                alert('Failed to save quest');
            }
        } catch (error) {
            alert('Invalid quest data: ' + error.message);
        }
    }
    
    editQuest(questId) {
        this.openQuestEditor(questId);
    }
    
    async deleteQuest(questId) {
        if (!confirm('Are you sure you want to delete this quest?')) return;
        
        try {
            await fetch(`/api/quests/${questId}`, {
                method: 'DELETE',
                headers: { 'Authorization': this.token }
            });
            
            await this.loadQuests();
            await this.loadNpcs();
        } catch (error) {
            alert('Failed to delete quest');
        }
    }
    
    addObjectiveField(data = null) {
        const container = document.getElementById('objectivesList');
        const item = document.createElement('div');
        item.className = 'objective-item';
        
        const type = data?.type || 'KILL';
        const targetId = data?.targetId || '';
        const required = data?.required || 1;
        
        item.innerHTML = `
            <button class="remove-btn" onclick="this.parentElement.remove()">×</button>
            <div class="form-group">
                <label>Type</label>
                <select class="obj-type" onchange="app.updateObjectiveFields(this)">
                    <option value="KILL" ${type === 'KILL' ? 'selected' : ''}>Kill Mobs</option>
                    <option value="TALK" ${type === 'TALK' ? 'selected' : ''}>Talk to NPC</option>
                    <option value="COLLECT" ${type === 'COLLECT' ? 'selected' : ''}>Collect Items</option>
                    <option value="VISIT" ${type === 'VISIT' ? 'selected' : ''}>Visit Location</option>
                </select>
            </div>
            <div class="form-row">
                <div class="form-group">
                    <label class="target-label">Target</label>
                    <input type="text" class="obj-target" value="${targetId}" placeholder="e.g., ZOMBIE, npc_123, DIAMOND">
                </div>
                <div class="form-group amount-group" style="display: ${type === 'KILL' || type === 'COLLECT' ? 'block' : 'none'}">
                    <label>Amount</label>
                    <input type="number" class="obj-amount" value="${required}" min="1">
                </div>
            </div>
        `;
        
        container.appendChild(item);
        this.updateObjectiveLabels(item.querySelector('.obj-type'));
    }
    
    updateObjectiveFields(select) {
        const item = select.closest('.objective-item');
        const type = select.value;
        const amountGroup = item.querySelector('.amount-group');
        
        // Show/hide amount field based on type
        if (type === 'KILL' || type === 'COLLECT') {
            amountGroup.style.display = 'block';
        } else {
            amountGroup.style.display = 'none';
        }
        
        this.updateObjectiveLabels(select);
    }
    
    updateObjectiveLabels(select) {
        const item = select.closest('.objective-item');
        const type = select.value;
        const targetLabel = item.querySelector('.target-label');
        const targetInput = item.querySelector('.obj-target');
        
        switch(type) {
            case 'KILL':
                targetLabel.textContent = 'Mob Type';
                targetInput.placeholder = 'e.g., ZOMBIE, SKELETON, CREEPER';
                break;
            case 'TALK':
                targetLabel.textContent = 'NPC ID';
                targetInput.placeholder = 'e.g., npc_merchant, npc_guard';
                break;
            case 'COLLECT':
                targetLabel.textContent = 'Item Material';
                targetInput.placeholder = 'e.g., DIAMOND, IRON_INGOT, WHEAT';
                break;
            case 'VISIT':
                targetLabel.textContent = 'Location ID';
                targetInput.placeholder = 'e.g., castle, village_center';
                break;
        }
    }
    
    addItemRewardField(data = null) {
        const container = document.getElementById('itemRewardsList');
        const item = document.createElement('div');
        item.className = 'item-reward-item';
        
        const material = data?.type || '';
        const amount = data?.amount || 1;
        
        item.innerHTML = `
            <button class="remove-btn" onclick="this.parentElement.remove()">×</button>
            <div class="form-row">
                <div class="form-group">
                    <label>Material</label>
                    <input type="text" class="item-material" value="${material}" placeholder="e.g., DIAMOND, IRON_SWORD">
                </div>
                <div class="form-group">
                    <label>Amount</label>
                    <input type="number" class="item-amount" value="${amount}" min="1">
                </div>
            </div>
        `;
        
        container.appendChild(item);
    }
    
    addCommandField(command = '') {
        const container = document.getElementById('commandsList');
        const item = document.createElement('div');
        item.className = 'command-item';
        
        item.innerHTML = `
            <button class="remove-btn" onclick="this.parentElement.remove()">×</button>
            <div class="form-group">
                <label>Command</label>
                <input type="text" class="command-input" value="${command}" placeholder="e.g., give {player} diamond 5">
            </div>
        `;
        
        container.appendChild(item);
    }
}

// Initialize app
const app = new AethorQuestsApp();
