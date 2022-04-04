import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DisplayComponent } from './component/display/display.component';
import { LoadingComponent } from './component/loading/loading.component';
import { StartComponent } from './component/start/start.component';

const routes: Routes = [
  { path: '', component: StartComponent },
  { path: 'loading', component: LoadingComponent },
  { path: ':shortUrl', component: DisplayComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
