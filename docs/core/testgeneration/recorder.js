// User Action Recorder for Feature File Generation
// A browser-based tool to record user actions and generate Gherkin feature files

class ActionRecorder {
  constructor() {
    this.isRecording = false;
    this.recordedActions = [];
    this.currentFeature = {
      name: '',
      description: '',
      scenarios: []
    };
    this.currentScenario = null;
    this.setupUI();
    this.setupEventListeners();
  }

  setupUI() {
    // Create recorder UI container
    const recorderUI = document.createElement('div');
    recorderUI.id = 'action-recorder';
    recorderUI.style.cssText = `
      position: fixed;
      bottom: 20px;
      right: 20px;
      width: 300px;
      background: #f8f9fa;
      border: 1px solid #dee2e6;
      border-radius: 5px;
      padding: 15px;
      box-shadow: 0 0 10px rgba(0,0,0,0.1);
      z-index: 10000;
      font-family: Arial, sans-serif;
    `;

    // Create recorder content
    recorderUI.innerHTML = `
      <h3 style="margin-top: 0; margin-bottom: 15px; color: #343a40;">Feature File Recorder</h3>
      
      <div style="margin-bottom: 15px;">
        <label for="feature-name" style="display: block; margin-bottom: 5px; font-weight: bold;">Feature Name:</label>
        <input type="text" id="feature-name" placeholder="Enter feature name" style="width: 100%; padding: 8px; border: 1px solid #ced4da; border-radius: 4px;">
      </div>
      
      <div style="margin-bottom: 15px;">
        <label for="feature-description" style="display: block; margin-bottom: 5px; font-weight: bold;">Description:</label>
        <textarea id="feature-description" placeholder="As a...\nI want to...\nSo that..." rows="3" style="width: 100%; padding: 8px; border: 1px solid #ced4da; border-radius: 4px;"></textarea>
      </div>
      
      <div style="margin-bottom: 15px;">
        <label for="scenario-name" style="display: block; margin-bottom: 5px; font-weight: bold;">Scenario Name:</label>
        <input type="text" id="scenario-name" placeholder="Enter scenario name" style="width: 100%; padding: 8px; border: 1px solid #ced4da; border-radius: 4px;">
      </div>
      
      <div style="display: flex; gap: 10px; margin-bottom: 15px;">
        <button id="start-recording" style="flex: 1; padding: 8px; background: #28a745; color: white; border: none; border-radius: 4px; cursor: pointer;">Start Recording</button>
        <button id="stop-recording" style="flex: 1; padding: 8px; background: #dc3545; color: white; border: none; border-radius: 4px; cursor: pointer;" disabled>Stop Recording</button>
      </div>
      
      <div style="margin-bottom: 15px;">
        <button id="generate-feature" style="width: 100%; padding: 8px; background: #007bff; color: white; border: none; border-radius: 4px; cursor: pointer;">Generate Feature File</button>
      </div>
      
      <div style="margin-bottom: 15px;">
        <button id="toggle-actions" style="width: 100%; padding: 8px; background: #6c757d; color: white; border: none; border-radius: 4px; cursor: pointer;">Show Recorded Actions</button>
      </div>
      
      <div id="actions-list" style="display: none; max-height: 150px; overflow-y: auto; margin-bottom: 15px; border: 1px solid #ced4da; border-radius: 4px; padding: 8px;"></div>
      
      <div id="generated-output" style="display: none; margin-top: 15px;">
        <label style="display: block; margin-bottom: 5px; font-weight: bold;">Generated Feature File:</label>
        <textarea id="output-text" rows="10" style="width: 100%; padding: 8px; border: 1px solid #ced4da; border-radius: 4px;" readonly></textarea>
        <button id="copy-output" style="width: 100%; margin-top: 10px; padding: 8px; background: #17a2b8; color: white; border: none; border-radius: 4px; cursor: pointer;">Copy to Clipboard</button>
      </div>
    `;

    document.body.appendChild(recorderUI);
  }

  setupEventListeners() {
    // Button event listeners
    document.getElementById('start-recording').addEventListener('click', () => this.startRecording());
    document.getElementById('stop-recording').addEventListener('click', () => this.stopRecording());
    document.getElementById('generate-feature').addEventListener('click', () => this.generateFeatureFile());
    document.getElementById('toggle-actions').addEventListener('click', () => this.toggleActionsList());
    document.getElementById('copy-output').addEventListener('click', () => this.copyToClipboard());

    // Form input event listeners
    document.getElementById('feature-name').addEventListener('input', (e) => {
      this.currentFeature.name = e.target.value;
    });
    
    document.getElementById('feature-description').addEventListener('input', (e) => {
      this.currentFeature.description = e.target.value;
    });
    
    document.getElementById('scenario-name').addEventListener('input', (e) => {
      if (this.currentScenario) {
        this.currentScenario.name = e.target.value;
      }
    });
  }

  startRecording() {
    const scenarioName = document.getElementById('scenario-name').value;
    if (!scenarioName) {
      alert('Please enter a scenario name before recording');
      return;
    }

    this.isRecording = true;
    this.currentScenario = {
      name: scenarioName,
      steps: []
    };
    this.recordedActions = [];
    
    document.getElementById('start-recording').disabled = true;
    document.getElementById('stop-recording').disabled = false;
    document.getElementById('scenario-name').disabled = true;
    
    // Add notification that recording is active
    const notification = document.createElement('div');
    notification.id = 'recording-notification';
    notification.style.cssText = `
      position: fixed;
      top: 10px;
      left: 50%;
      transform: translateX(-50%);
      background: rgba(220, 53, 69, 0.9);
      color: white;
      padding: 10px 20px;
      border-radius: 5px;
      z-index: 10001;
    `;
    notification.textContent = '● Recording user actions...';
    document.body.appendChild(notification);
    
    // Set up event listeners for user actions
    this.setupActionEventListeners();
  }

  stopRecording() {
    this.isRecording = false;
    document.getElementById('start-recording').disabled = false;
    document.getElementById('stop-recording').disabled = true;
    document.getElementById('scenario-name').disabled = false;
    
    // Remove recording notification
    const notification = document.getElementById('recording-notification');
    if (notification) {
      notification.remove();
    }
    
    // Remove action event listeners
    this.removeActionEventListeners();
    
    // Process recorded actions into steps
    this.processRecordedActions();
    
    // Add this scenario to the feature
    this.currentFeature.scenarios.push(this.currentScenario);
    
    // Update the actions list display
    this.updateActionsList();
  }

  setupActionEventListeners() {
    // Click events
    document.addEventListener('click', this.handleClick.bind(this), true);
    
    // Input events
    document.addEventListener('input', this.handleInput.bind(this), true);
    
    // Form submission
    document.addEventListener('submit', this.handleSubmit.bind(this), true);
    
    // Navigation
    window.addEventListener('popstate', this.handleNavigation.bind(this));
    
    // Select changes
    document.addEventListener('change', this.handleChange.bind(this), true);
  }

  removeActionEventListeners() {
    document.removeEventListener('click', this.handleClick.bind(this), true);
    document.removeEventListener('input', this.handleInput.bind(this), true);
    document.removeEventListener('submit', this.handleSubmit.bind(this), true);
    window.removeEventListener('popstate', this.handleNavigation.bind(this));
    document.removeEventListener('change', this.handleChange.bind(this), true);
  }

  handleClick(event) {
    // Skip if the click is within our recorder UI
    if (event.target.closest('#action-recorder')) {
      return;
    }
    
    const element = event.target;
    const elementInfo = this.getElementInfo(element);
    
    this.recordedActions.push({
      type: 'click',
      element: elementInfo,
      timestamp: new Date().getTime()
    });
  }

  handleInput(event) {
    // Skip if the input is within our recorder UI
    if (event.target.closest('#action-recorder')) {
      return;
    }
    
    const element = event.target;
    const elementInfo = this.getElementInfo(element);
    
    this.recordedActions.push({
      type: 'input',
      element: elementInfo,
      value: element.value,
      timestamp: new Date().getTime()
    });
  }

  handleSubmit(event) {
    const form = event.target;
    
    this.recordedActions.push({
      type: 'submit',
      formId: form.id || 'unknown-form',
      formAction: form.action,
      timestamp: new Date().getTime()
    });
  }

  handleNavigation() {
    this.recordedActions.push({
      type: 'navigation',
      url: window.location.href,
      timestamp: new Date().getTime()
    });
  }

  handleChange(event) {
    // Skip if the change is within our recorder UI
    if (event.target.closest('#action-recorder')) {
      return;
    }
    
    const element = event.target;
    if (element.tagName === 'SELECT') {
      const elementInfo = this.getElementInfo(element);
      
      this.recordedActions.push({
        type: 'select',
        element: elementInfo,
        value: element.value,
        text: element.options[element.selectedIndex].text,
        timestamp: new Date().getTime()
      });
    }
  }

  getElementInfo(element) {
    // Get useful information about the element to identify it
    const tagName = element.tagName.toLowerCase();
    const id = element.id || '';
    const classNames = Array.from(element.classList).join(' ');
    const name = element.name || '';
    const type = element.type || '';
    const text = element.textContent ? element.textContent.trim().substring(0, 50) : '';
    const placeholder = element.placeholder || '';
    const value = element.value || '';
    const href = element.href || '';
    
    // Try to get a descriptive label for the element
    let label = '';
    if (id) {
      const labelElement = document.querySelector(`label[for="${id}"]`);
      if (labelElement) {
        label = labelElement.textContent.trim();
      }
    }
    
    return {
      tagName,
      id,
      classNames,
      name,
      type,
      text,
      placeholder,
      value,
      href,
      label
    };
  }

  processRecordedActions() {
    if (this.recordedActions.length === 0) {
      return;
    }
    
    // First action is usually a "Given" condition
    const firstAction = this.recordedActions[0];
    this.currentScenario.steps = [];
    
    // Add the initial state as a "Given" step
    this.currentScenario.steps.push({
      type: 'Given',
      text: `I am on the ${document.title} page`
    });
    
    // Process the rest of the actions as "When" steps
    this.recordedActions.forEach((action, index) => {
      if (index === 0 && action.type === 'navigation') {
        return; // Skip the first navigation action as it's covered by the "Given" step
      }
      
      const step = this.convertActionToStep(action, index);
      if (step) {
        // First action after the "Given" should be a "When", the rest should be "And"
        if (this.currentScenario.steps.length === 1) {
          step.type = 'When';
        } else {
          step.type = 'And';
        }
        this.currentScenario.steps.push(step);
      }
    });
    
    // Add a "Then" step as a placeholder for the expected result
    this.currentScenario.steps.push({
      type: 'Then',
      text: 'I should see the expected result'
    });
  }

  convertActionToStep(action, index) {
    switch (action.type) {
      case 'click':
        return this.createClickStep(action);
      case 'input':
        return this.createInputStep(action);
      case 'submit':
        return this.createSubmitStep(action);
      case 'navigation':
        return this.createNavigationStep(action);
      case 'select':
        return this.createSelectStep(action);
      default:
        return null;
    }
  }

  createClickStep(action) {
    const element = action.element;
    let text = '';
    
    if (element.text) {
      text = `I click on "${element.text}"`;
    } else if (element.label) {
      text = `I click on the "${element.label}" element`;
    } else if (element.id) {
      text = `I click on the element with id "${element.id}"`;
    } else if (element.type === 'button' || element.type === 'submit') {
      text = `I click the ${element.value || 'button'}`;
    } else if (element.tagName === 'a') {
      text = `I click on the link to "${element.href}"`;
    } else {
      text = `I click on the ${element.tagName} element`;
    }
    
    return { text };
  }

  createInputStep(action) {
    const element = action.element;
    let text = '';
    
    if (element.type === 'checkbox') {
      text = `I ${action.value ? 'check' : 'uncheck'} the "${element.label || element.id || 'checkbox'}"`;
    } else if (element.type === 'radio') {
      text = `I select the "${element.label || element.value || 'radio'}" option`;
    } else {
      const fieldName = element.label || element.placeholder || element.id || element.name || 'field';
      text = `I enter "${action.value}" in the "${fieldName}"`;
    }
    
    return { text };
  }

  createSubmitStep(action) {
    return { text: `I submit the form` };
  }

  createNavigationStep(action) {
    return { text: `I navigate to "${action.url}"` };
  }

  createSelectStep(action) {
    const element = action.element;
    const fieldName = element.label || element.id || element.name || 'dropdown';
    return { text: `I select "${action.text}" from the "${fieldName}"` };
  }

  updateActionsList() {
    const actionsListEl = document.getElementById('actions-list');
    actionsListEl.innerHTML = '';
    
    if (this.currentScenario && this.currentScenario.steps) {
      this.currentScenario.steps.forEach(step => {
        const stepEl = document.createElement('div');
        stepEl.style.marginBottom = '5px';
        stepEl.style.paddingBottom = '5px';
        stepEl.style.borderBottom = '1px solid #eee';
        stepEl.textContent = `${step.type} ${step.text}`;
        actionsListEl.appendChild(stepEl);
      });
    }
  }

  toggleActionsList() {
    const actionsListEl = document.getElementById('actions-list');
    const toggleButton = document.getElementById('toggle-actions');
    
    if (actionsListEl.style.display === 'none') {
      actionsListEl.style.display = 'block';
      toggleButton.textContent = 'Hide Recorded Actions';
    } else {
      actionsListEl.style.display = 'none';
      toggleButton.textContent = 'Show Recorded Actions';
    }
  }

  generateFeatureFile() {
    const featureName = this.currentFeature.name;
    if (!featureName) {
      alert('Please enter a feature name');
      return;
    }
    
    if (this.currentFeature.scenarios.length === 0) {
      alert('Please record at least one scenario');
      return;
    }
    
    let featureFile = `Feature: ${featureName}\n`;
    
    // Add description if provided
    if (this.currentFeature.description) {
      const descLines = this.currentFeature.description.split('\n');
      descLines.forEach(line => {
        featureFile += `  ${line}\n`;
      });
      featureFile += '\n';
    }
    
    // Add scenarios
    this.currentFeature.scenarios.forEach(scenario => {
      featureFile += `  Scenario: ${scenario.name}\n`;
      
      // Add steps
      scenario.steps.forEach(step => {
        featureFile += `    ${step.type} ${step.text}\n`;
      });
      
      featureFile += '\n';
    });
    
    // Show the generated output
    document.getElementById('output-text').value = featureFile;
    document.getElementById('generated-output').style.display = 'block';
  }

  copyToClipboard() {
    const outputText = document.getElementById('output-text');
    outputText.select();
    document.execCommand('copy');
    
    // Show feedback
    const copyButton = document.getElementById('copy-output');
    const originalText = copyButton.textContent;
    copyButton.textContent = 'Copied!';
    setTimeout(() => {
      copyButton.textContent = originalText;
    }, 2000);
  }
}

// Initialize the recorder when the script is loaded
window.addEventListener('load', () => {
  window.actionRecorder = new ActionRecorder();
  console.log('Action Recorder initialized. Start recording user actions to generate feature files.');
});

// Export the ActionRecorder class for module usage
if (typeof module !== 'undefined' && module.exports) {
  module.exports = { ActionRecorder };
}