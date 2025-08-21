/* Auth page interactions */
const $ = (s, r=document) => r.querySelector(s);
const $$ = (s, r=document) => Array.from(r.querySelectorAll(s));

const signinTab = $("#tab-signin");
const signupTab = $("#tab-signup");
const signinForm = $("#form-signin");
const signupForm = $("#form-signup");

function setActive(tab){
  if(tab === 'signin'){
    signinTab.classList.add('active');
    signupTab.classList.remove('active');
    signinForm.hidden = false;
    signupForm.hidden = true;
  } else {
    signupTab.classList.add('active');
    signinTab.classList.remove('active');
    signupForm.hidden = false;
    signinForm.hidden = true;
  }
}

signinTab.addEventListener('click', ()=> setActive('signin'));
signupTab.addEventListener('click', ()=> setActive('signup'));

function showError(inputEl, msg){
  const err = inputEl.closest('.field').querySelector('.err');
  if(err) err.textContent = msg || '';
}

function clearErrors(form){
  $$('.err', form).forEach(e=> e.textContent = '');
}

function validateEmail(email){
    return /.+@.+\..+/.test(email);
}

async function doSignin(username, password){
  const res = await fetch('/api/auth/public/signin', {
    method:'POST', headers:{'Content-Type':'application/json'},
    body: JSON.stringify({ username, password })
  });
  if(!res.ok){
    const t = await res.json().catch(()=>({message:'Login failed'}));
    throw new Error(t.message || 'Login failed');
  }
  const data = await res.json();
  // store token for subsequent API calls
  localStorage.setItem('token', data.token);
  localStorage.setItem('username', data.username);
  localStorage.setItem('roles', JSON.stringify(data.roles||[]));
}

signinForm.addEventListener('submit', async (e)=>{
  e.preventDefault();
  clearErrors(signinForm);
  const username = $('#signin-username').value.trim();
  const password = $('#signin-password').value;
  let ok = true;
  if(!username){ showError($('#signin-username'), 'Username is required'); ok=false; }
  if(!password){ showError($('#signin-password'), 'Password is required'); ok=false; }
  if(!ok) return;
  const btn = $('#btn-signin');
  btn.disabled = true; btn.textContent = 'Signing in...';
  try{
     await doSignin(username, password);
     window.location.href = '/dashboard.html';
  }catch(err){
     showError($('#signin-password'), err.message);
  }finally{
     btn.disabled = false; btn.textContent = 'Sign In';
  }
});

signupForm.addEventListener('submit', async (e)=>{
  e.preventDefault();
  clearErrors(signupForm);
  const fullName = $('#signup-fullname').value.trim();
  const email = $('#signup-email').value.trim();
  const password = $('#signup-password').value;
  const confirm = $('#signup-confirm').value;
  let ok = true;
  if(!fullName){ showError($('#signup-fullname'), 'Full name is required'); ok=false; }
  const username = fullName.replace(/\s+/g,'').toLowerCase();
  if(!email || !validateEmail(email)){ showError($('#signup-email'), 'Valid email is required'); ok=false; }
  if(!password || password.length < 6){ showError($('#signup-password'), 'Min 6 characters'); ok=false; }
  if(confirm !== password){ showError($('#signup-confirm'), 'Passwords do not match'); ok=false; }
  if(!ok) return;
  const btn = $('#btn-signup');
  btn.disabled = true; btn.textContent = 'Creating account...';
  try{
     const res = await fetch('/api/auth/public/signup', {
       method:'POST', headers:{'Content-Type':'application/json'},
       body: JSON.stringify({ username, email, password })
     });
     if(!res.ok){
       const t = await res.json().catch(()=>({message:'Sign up failed'}));
       throw new Error(t.message || 'Sign up failed');
     }
     // auto sign-in after successful sign up
     await doSignin(username, password);
     window.location.href = '/dashboard.html';
  }catch(err){
     showError($('#signup-email'), err.message);
  }finally{
     btn.disabled = false; btn.textContent = 'Sign Up';
  }
});

// Social buttons - placeholders
$$('.sbtn').forEach(btn=>{
  btn.addEventListener('click', ()=>{
    alert('Social login not configured in backend. This is a placeholder.');
  });
});

// Default view
setActive('signin');
