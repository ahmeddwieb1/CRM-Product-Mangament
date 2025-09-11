/* Auth page interactions */
const $ = (s, r=document) => r.querySelector(s);
const $$ = (s, r=document) => Array.from(r.querySelectorAll(s));

const signinTab = $("#tab-signin");
const signinForm = $("#form-signin");

function setActive(tab){
  if(tab === 'signin'){
    signinTab.classList.add('active');
    signinForm.hidden = false;
  } else {
    signinTab.classList.remove('active');
    signinForm.hidden = true;
  }
}

signinTab.addEventListener('click', ()=> setActive('signin'));

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

// Social buttons - placeholders
$$('.sbtn').forEach(btn=>{
  btn.addEventListener('click', ()=>{
    alert('Social login not configured in backend. This is a placeholder.');
  });
});

// Default view
setActive('signin');
