using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Navigation;
using Microsoft.Phone.Controls;
using Microsoft.Phone.Shell;
using System.IO.IsolatedStorage;
using guess_the_number_wp.Resources;

namespace guess_the_number_wp
{
    public partial class MainPage : PhoneApplicationPage
    {
        private int myNumber, userNumber, guessCount = 0;
        private Random random;
        private IsolatedStorageSettings settings = IsolatedStorageSettings.ApplicationSettings;

        // Constructor
        public MainPage()
        {
            InitializeComponent();

            if (!settings.Contains("best"))
            {
                settings.Add("best", 0);
                updateLabels(guessCount, 0);
                settings.Save();
            }
            else
            {
                updateLabels(guessCount, Int32.Parse(settings["best"].ToString()));
            }

            random = new Random();

            newGame(null, null);

        }
        
        private void updateLabels(int guessCount, int bestScore)
        {
            times_guessed.Text = AppResources.times_guessed + ": " + guessCount;
            if (bestScore != -1)
            {
                best_score.Text = AppResources.best_score + ": " + bestScore;
            }

        }

        private void takeTheGuess(object sender, RoutedEventArgs e)
        {
            if (number.Text != " ")
            {
                guessCount++;
                updateLabels(guessCount, -1);
                int newNumber = Int32.Parse(number.Text);

                userNumber = newNumber;

                if (userNumber > myNumber)
                {
                    MessageBox.Show(AppResources.my_number_is_less_tan_yours);
                }
                else if (userNumber < myNumber)
                {
                    MessageBox.Show(AppResources.my_number_is_bigger_than_yours);
                }
                else if (userNumber == myNumber)
                {
                    MessageBox.Show(AppResources.congeats_you_gussed_my_number);
                    if (guessCount < Int32.Parse(settings["best"].ToString()) || Int32.Parse(settings["best"].ToString()) == 0)
                    {
                        settings["best"] = guessCount;
                        updateLabels(guessCount, guessCount);
                        settings.Save();
                    }
                    newGame(sender, e);
                }
            }


            //MessageBox.Show("moja " + tmp + " wylosowana " + myNumber);
        }

        private void newGame(object sender, RoutedEventArgs e)
        {
            myNumber = random.Next(0, 100);
            guessCount = 0;
            updateLabels(guessCount, -1);
            number.Text = "";
        }

  
        
    }
}