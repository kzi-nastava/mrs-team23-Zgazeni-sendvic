import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { authInterceptor } from './app/jwt/auth-interceptor';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

bootstrapApplication(App, {
  ...appConfig,
  providers: [
    ...(appConfig.providers || []),
    provideHttpClient(
      withInterceptors([authInterceptor]) // <-- attach your interceptor here
    )
  ]
})
  .catch(err => console.error(err));
